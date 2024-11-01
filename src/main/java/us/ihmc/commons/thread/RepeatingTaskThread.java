package us.ihmc.commons.thread;

import us.ihmc.commons.Conversions;
import us.ihmc.commons.RunnableThatThrows;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.exception.ExceptionHandler;
import us.ihmc.commons.exception.ExceptionTools;

/**
 * A thread that executes code in a loop.
 * <p>
 * This thread has 3 states: {@link #REPEAT_INDEFINITELY},
 * looping for a set number of repetitions, and paused (when remaining repetitions = 0).
 * <p>
 * Upon construction, the thread will not be alive, and will have zero remaining repetitions to run.
 * To start repeating the task, the number of repetitions must be set and {@link #start()} must be called
 * (the order does not matter). Alternatively, you may call {@link #startRepeating()}, which will
 * signal the thread to repeat indefinitely, and start the thread if it has not been started.
 * Once the thread runs the task for the set number of repetitions,
 * it will pause and wait until the number of remaining repetitions is changed.
 * <p>
 * This thread does not finish running until {@link #kill()} or {@link #blockingKill()} is called.
 * Once started, be sure to kill this thread.
 * <p>
 * If this thread is interrupted while paused, a repetition will run such that the interrupted status
 * can be handled by user code (i.e. code in the passed in task, or overridden {@link #repeat()} method).
 * <p>
 * Optionally, you may set a limit to the loop frequency through {@link #setFrequencyLimit(double)},
 * or by passing the frequency limit as a parameter in the constructor.
 * The loop frequency limit may be changed at any time. To de-limit the loop frequency,
 * use {@link #removeFrequencyLimit()}, or set the limit < 0.0.
 * Setting the frequency limit only guarantees that the loop's frequency will not exceed the limit.
 * It does not guarantee that the loop will run at the set frequency, as the code executed within the loop
 * may be too slow to run at that frequency.
 * <p>
 * Like {@link Thread}, the {@link RepeatingTaskThread} may be used in two ways:
 * <ul>
 *    <li>
 *       First, by passing in a runnable (or in this case a {@link RunnableThatThrows}) to the constructor.
 *       This runnable will be called in {@link #repeat()} every repetition.
 *    <li>
 *       Second, by {@code @Override}ing the {@link #repeat()} method.
 *       The code within {@link #repeat()} will run every repetition.
 */
public class RepeatingTaskThread extends Thread
{
   public static final int REPEAT_INDEFINITELY = -1;
   public static final double UNLIMITED_FREQUENCY = -1.0;

   private final RunnableThatThrows task;
   private final ExceptionHandler exceptionHandler;
   private final Object loopLock = new Object();

   /** Throttler for optionally set loop period/frequency limit */
   private final Throttler throttler = new Throttler();

   /**
    * Countdown for number of repetitions to run.
    * The counter is decremented each time before the repetition.
    * Once the counter hits 0, the loop is paused.
    * <ul>
    *    <li> 0 = pause (don't run the loop until counter value is changed).
    *    <li> -1 = loop indefinitely (keep looping until told otherwise).
    *    <li> N > 0 = run the loop N more repetitions.
    */
   private volatile int remainingRepetitions = 0;

   /** Counter for the total number of repetitions completed during the lifetime of this thread. */
   private long completedRepetitions = 0L;

   /**
    * Indicates whether this object is destroyed.
    * The loop will come to a finish when {@code isDestroyed == true}.
    * Does not equal to {@link Thread#isAlive()}, as the thread may take
    * some time to finish executing after {@code isDestroyed} becomes true.
    */
   private volatile boolean running = false;

   /** The optionally set lower limit to the loop period. A zero or negative value indicates no limit */
   private volatile double loopPeriodLowerLimit = UNLIMITED_FREQUENCY;

   public RepeatingTaskThread(String name)
   {
      this(UNLIMITED_FREQUENCY, name);
   }

   public RepeatingTaskThread(double loopFrequencyLimit, String name)
   {
      this(DefaultExceptionHandler.MESSAGE_AND_STACKTRACE, loopFrequencyLimit, name);
   }

   public RepeatingTaskThread(ExceptionHandler exceptionHandler, String name)
   {
      this(exceptionHandler, UNLIMITED_FREQUENCY, name);
   }

   public RepeatingTaskThread(ExceptionHandler exceptionHandler, double loopFrequencyLimit, String name)
   {
      this(null, exceptionHandler, loopFrequencyLimit, name);
   }

   public RepeatingTaskThread(RunnableThatThrows task, String name)
   {
      this(task, UNLIMITED_FREQUENCY, name);
   }

   public RepeatingTaskThread(RunnableThatThrows task, double loopFrequencyLimit, String name)
   {
      this(task, DefaultExceptionHandler.MESSAGE_AND_STACKTRACE, loopFrequencyLimit, name);
   }

   public RepeatingTaskThread(RunnableThatThrows task, ExceptionHandler exceptionHandler, String name)
   {
      this(task, exceptionHandler, UNLIMITED_FREQUENCY, name);
   }

   public RepeatingTaskThread(RunnableThatThrows task, ExceptionHandler exceptionHandler, double loopFrequencyLimit, String name)
   {
      super(name);
      this.task = task;
      this.exceptionHandler = exceptionHandler;
      setFrequencyLimit(loopFrequencyLimit);
   }

   /**
    * Limit the frequency of the loop execution.
    * To un-limit the loop frequency, pass in a number less than or equal to 0.0.
    * <p>
    * Setting the frequency limit only guarantees that the loop's frequency will not exceed the limit.
    * It does not guarantee that the loop will run AT the set frequency, as the code executed within the loop
    * may be too slow to run at that frequency.
    *
    * @param frequencyLimit The limit for the loop frequency.
    *                       If zero or negative, the loop's frequency is not limited.
    */
   public void setFrequencyLimit(double frequencyLimit)
   {
      loopPeriodLowerLimit = Conversions.hertzToSeconds(frequencyLimit);
   }

   /**
    * Removes any limit to the loop frequency that may have been set.
    * Equivalent to calling {@code setFrequencyLimit(-1.0)}.
    */
   public void removeFrequencyLimit()
   {
      setFrequencyLimit(UNLIMITED_FREQUENCY);
   }

   @Override
   public void start()
   {
      running = true;
      super.start();
   }

   /**
    * Signals the thread to start repeating indefinitely.
    * If the thread has not been started yet, calling this
    * method will also start the thread.
    */
   public void startRepeating()
   {
      if (!isAlive())
         start();

      setRepeating(true);
   }

   /**
    * Signals the thread to stop repeating once the current repetition finishes.
    * The thread will be paused until the value of remaining repetitions changes, or the thread is killed.
    */
   public void stopRepeating()
   {
      setRepeating(false);
   }

   /**
    * Signal the thread to repeat indefinitely, or to stop repeating.
    *
    * @param repeating Whether the thread should be repeating.
    */
   public void setRepeating(boolean repeating)
   {
      if (repeating)
         setRemaining(REPEAT_INDEFINITELY);
      else
         setRemaining(0);
   }

   /**
    * Signal the thread to loop for the passed in number of repetitions.
    * This overrides the remaining number of repetitions, regardless of its previous value.
    * <p>
    * This method also accepts {@link #REPEAT_INDEFINITELY}.
    * <p>
    * To add or subtract to the number of repetitions the thread should loop, use {@link #addRemaining(int)}.
    *
    * @param repetitions The number of repetitions the thread should loop after this call.
    */
   public void setRemaining(int repetitions)
   {
      synchronized (loopLock)
      {
         this.remainingRepetitions = repetitions;
         loopLock.notify();
      }
   }

   /**
    * Add N repetitions to the remaining repetition counter.
    * If the thread was paused, adding repetitions begins the loop.
    * <p>
    * You may also subtract from the number of remaining repetitions by passing in a negative number.
    * If the resulting number of repetitions is 0, the loop will be paused.
    * This method cannot cause the remaining repetition count to go below 0.
    * <p>
    * This method does not do anything if the thread is repeating indefinitely.
    *
    * @param repetitions The number of repetitions to add. This can be a negative value for subtraction.
    */
   public void addRemaining(int repetitions)
   {
      synchronized (loopLock)
      {
         // If looping indefinitely, do nothing
         if (remainingRepetitions < 0)
            return;

         // Add to the remaining repetition counter
         remainingRepetitions += repetitions;

         // Ensure remaining repetition counter doesn't become negative in case of subtraction
         if (remainingRepetitions < 0)
            remainingRepetitions = 0;

         loopLock.notify();
      }
   }

   /**
    * Get the remaining number of repetitions this thread plans to run.
    *
    * @return The remaining number of loops this thread plans to run.
    */
   public int getRemainingRepetitions()
   {
      return remainingRepetitions;
   }

   /**
    * Whether this thread is running. In other words, whether this thread has not been {@link #kill()}ed.
    * <p>
    * The returned value of this method does not necessarily equate to {@link #isAlive()},
    * as the loop may take some time to finish after the call to {@link #kill()},
    * during which the thread remains alive.
    *
    * @return {@code false} if {@link #kill()} or {@link #blockingKill()} has been called. {@code true} otherwise.
    */
   public boolean isRunning()
   {
      return running;
   }

   /**
    * Whether this thread is currently looping.
    *
    * @return {@code true} if the thread is looping. {@code false} if the thread is paused or killed.
    */
   public synchronized boolean isLooping()
   {
      if (!isRunning())
         return false;

      return remainingRepetitions != 0;
   }

   /**
    * Signals the thread to stop once the current repetition finishes running.
    * The loop will be exited, and the thread will die.
    * The thread cannot be restarted after calling this method.
    */
   public void kill()
   {
      synchronized (loopLock)
      {
         running = false;
         loopLock.notify();
      }
   }

   /**
    * Signals the thread to stop once the current repetition finishes running.
    * The loop will be exited, and the thread will die.
    * Same as calling {@link #kill()} then {@link #join()}.
    * InterruptedExceptions are ignored. To handle interrupted exceptions,
    * call {@link #kill()} then {@link #join()} manually.
    */
   public void blockingKill()
   {
      kill();
      try
      {
         join();
      }
      catch (InterruptedException ignored) {}
   }

   /**
    * The method that is executed repeatedly in a loop.
    * <p>
    * You may {@code @Override} this method with the code to repeat.
    * Alternatively, if a {@link RunnableThatThrows} was passed it, this method
    * will call the run method in the loop.
    *
    * @throws Throwable Any throwable that the overriding code or the passed in task may throw.
    *       This throwable will be handled by the passed in {@link ExceptionHandler}
    *       (by default it is {@link DefaultExceptionHandler#MESSAGE_AND_STACKTRACE}).
    */
   protected void repeat() throws Throwable
   {
      if (task != null)
         task.run();
   }

   /**
    * The {@link Thread#run()} method, overridden to run a loop.
    * To extend this class {@link Thread} style, override {@link #repeat()} instead.
    * <p>
    * DO NOT CALL THIS METHOD. Well, you can, but why would you?
    * You are using a thread to run things asynchronously, but calling this would run the loop synchronously.
    * Why would you want that?
    * <p>
    * This method is a necessary evil committed for this class to extend Thread.
    */
   @Override
   public final void run()
   {
      while (running)
      {
         try
         {
            synchronized (loopLock)
            {  // No more runs remaining -> wait until something changes
               if (remainingRepetitions == 0)
               {
                  loopLock.wait();
                  continue;
               }

               // Decrement the counter for the run that's about to occur
               if (remainingRepetitions > 0)
                  remainingRepetitions--;
            }

            // If a period/frequency limit was set, wait until loop can run.
            if (loopPeriodLowerLimit > 0.0)
            {
               /*
                * This call must not swallow interrupts.
                * As of writing this comment (Nov, 2024), LockSupport.parkNanos() is used internally to block.
                * Although the throttler will block until the period has elapsed, the thread
                * remains interrupted.
                */
               throttler.waitAndRun(loopPeriodLowerLimit);
            }
         }
         catch (InterruptedException interrupted)
         {  // Maintain interrupted status so that runInLoop can handle it
            interrupt();
         }

         // Run the runInLoop method, and handle any exception it may throw.
         ExceptionTools.handle(this::repeat, exceptionHandler);
         ++completedRepetitions;
      }
   }
}
