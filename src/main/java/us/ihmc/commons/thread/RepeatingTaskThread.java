package us.ihmc.commons.thread;

import us.ihmc.commons.Conversions;
import us.ihmc.commons.RunnableThatThrows;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.exception.ExceptionHandler;
import us.ihmc.commons.exception.ExceptionTools;

/**
 * A thread that repeats execution of a single task. It can do this N times, continuously, or at a constant rate.
 * <p>
 * This thread has 3 states: {@link #REPEAT_INDEFINITELY},
 * looping for a set number of repetitions, and paused (when remaining repetitions = 0).
 * <p>
 * Upon construction, the thread will have zero remaining repetitions to run.
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
 * can be handled by user code (i.e. code in the passed in task, or overridden {@link #runTask()} method).
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
 *       This runnable will be called in {@link #runTask()} every repetition.
 *    <li>
 *       Second, by {@code @Override}ing the {@link #runTask()} method.
 *       The code within {@link #runTask()} will run every repetition.
 */
public class RepeatingTaskThread extends Thread
{
   public static final int REPEAT_INDEFINITELY = -1;
   public static final double UNLIMITED_FREQUENCY = -1.0;

   private final RunnableThatThrows task;
   private final ExceptionHandler exceptionHandler;

   /** State of this thread */
   private final State state = new State();

   /** Throttler for optionally set loop period/frequency limit */
   private final Throttler throttler = new Throttler();

   /** The optionally set lower limit to the loop period. A negative value indicates no limit */
   private volatile double loopPeriodLowerLimit = UNLIMITED_FREQUENCY;

   public RepeatingTaskThread(String name)
   {
      this(DefaultExceptionHandler.MESSAGE_AND_STACKTRACE, name);
   }

   public RepeatingTaskThread(ExceptionHandler exceptionHandler, String name)
   {
      this(null, exceptionHandler, name);
   }

   public RepeatingTaskThread(RunnableThatThrows task, String name)
   {
      this(task, DefaultExceptionHandler.MESSAGE_AND_STACKTRACE, name);
   }

   public RepeatingTaskThread(RunnableThatThrows task, ExceptionHandler exceptionHandler, String name)
   {
      super(name);
      this.task = task;
      this.exceptionHandler = exceptionHandler;
   }

   /**
    * Limit the frequency of the repetition execution.
    * To un-limit the repetition frequency, use {@link #removeFrequencyLimit()},
    * or pass in {@link #UNLIMITED_FREQUENCY} (any value less than 0.0 will work).
    * <p>
    * Setting the frequency limit only guarantees that the loop's frequency will not exceed the limit.
    * It does not guarantee that the loop will run AT the set frequency, as the code executed within
    * each repetition may be too slow to run at that frequency.
    *
    * @param frequencyLimit The limit for the loop frequency. If negative, the loop's frequency is not limited.
    * @return {@code this}, such that it can be used like a
    *       <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>.
    */
   public RepeatingTaskThread setFrequencyLimit(double frequencyLimit)
   {
      loopPeriodLowerLimit = Conversions.hertzToSeconds(frequencyLimit);
      return this;
   }

   /**
    * Removes any limit to the loop frequency that may have been set.
    * Equivalent to calling {@code setFrequencyLimit(UNLIMITED_FREQUENCY)}.
    */
   public void removeFrequencyLimit()
   {
      setFrequencyLimit(UNLIMITED_FREQUENCY);
   }

   @Override
   public void start()
   {
      state.setRunning(true);
      super.start();
   }

   /**
    * Signals the thread to start repeating indefinitely.
    * If the thread has not been started yet, calling this
    * method will also start the thread.
    */
   public void startRepeating()
   {
      if (!state.isRunning())
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
   public void setRemaining(long repetitions)
   {
      state.setRemaining(repetitions);
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
      state.addRemaining(repetitions);
   }

   /**
    * Access the state of this thread.
    *
    * @return The state of this thread.
    */
   public State getRepetitionState()
   {
      return state;
   }

   /**
    * Signals the thread to stop once the current repetition finishes running.
    * The loop will be exited, and the thread will die.
    * The thread cannot be restarted after calling this method.
    */
   public void kill()
   {
      state.setRunning(false);
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
   protected void runTask() throws Throwable
   {
      if (task != null)
         task.run();
   }

   /**
    * The {@link Thread#run()} method, overridden to run a loop.
    * To extend this class {@link Thread} style, override {@link #runTask()} instead.
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
      while (state.isRunning())
      {
         try
         {
            synchronized (state)
            {
               if (state.getRemaining() == 0L)
               {
                  state.waitForChange();
                  continue;
               }
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
         {  // Maintain interrupted status so that runTask method can handle it
            interrupt();
         }

         // Run the runTask method, and handle any exception it may throw.
         state.beforeTaskExecution();
         ExceptionTools.handle(this::runTask, exceptionHandler);
         state.afterTaskExecution();
      }
   }

   /**
    * Whether this thread is currently looping. Used for testing.
    *
    * @return {@code true} if the thread is looping.
    *       {@code false} if the thread is paused, killed, or hasn't been started.
    */
   /* package-private */ synchronized boolean isRepeating()
   {
      if (!state.isRunning())
         return false;

      return state.getRemaining() != 0L;
   }

   /** The state of the RepeatingTaskThread. */
   public static class State
   {
      /**
       * Becomes {@code true} when the thread is started, and {@code false} when the thread is killed.
       * Once {@code false}, the task loop will allow the currently executing task (if any) to complete,
       * and the task loop is exited, allowing the thread to die.
       */
      private boolean running = false;

      /**
       * Countdown for number of repetitions to run.
       * The counter is decremented each time after the repetition.
       * Once the counter hits 0, the loop is paused.
       * <ul>
       *    <li> 0 = pause (don't run the loop until counter value is changed).
       *    <li> -1 = repeat indefinitely (keep looping until told otherwise).
       *    <li> N > 0 = run the loop N more repetitions.
       */
      private long remainingRepetitions = 0L;

      /** Whether a task is currently executing */
      private boolean executing = false;

      /** Counter for the total number of repetitions completed during the lifetime of this thread. */
      private long completedRepetitions = 0L;

      /** Call right before executing a task */
      private synchronized void beforeTaskExecution()
      {
         executing = true;
         this.notifyAll();
      }

      /** Call right after executing a task */
      private synchronized void afterTaskExecution()
      {
         executing = false;
         if (remainingRepetitions > 0)
            remainingRepetitions--;
         completedRepetitions++;
         this.notifyAll();
      }

      /**
       * Set whether the thread is running.
       * Should become {@code true} when the thread is started,
       * and {@code false} when the thread is signalled to die.
       *
       * @param running Whether the thread is/should be running.
       */
      private synchronized void setRunning(boolean running)
      {
         this.running = running;
         this.notifyAll();
      }

      /**
       * Get whether the thread is running.
       * @return Whether the thread is running.
       */
      private synchronized boolean isRunning()
      {
         return running;
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
      private synchronized void setRemaining(long repetitions)
      {
         remainingRepetitions = repetitions;
         this.notifyAll();
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
      private synchronized void addRemaining(int repetitions)
      {
         // If repeating indefinitely, do nothing
         if (remainingRepetitions < 0L)
            return;

         // Add to the remaining repetition counter
         remainingRepetitions += repetitions;

         // Ensure remaining repetition counter doesn't become negative in case of subtraction
         if (remainingRepetitions < 0L)
            remainingRepetitions = 0L;

         this.notifyAll();
      }

      /**
       * Wait until a change occurs to the thread's state.
       *
       * @throws InterruptedException If the waiting thread is interrupted.
       */
      public synchronized void waitForChange() throws InterruptedException
      {
         this.wait();
      }

      /**
       * Wait until the next start of a task.
       *
       * @throws InterruptedException If the waiting thread is interrupted.
       */
      public synchronized void waitForNextTaskStart() throws InterruptedException
      {
         do
         {
            waitForChange();
         } while (!executing);
      }

      /**
       * Wait until the next end of a task.
       *
       * @throws InterruptedException If the waiting thread is interrupted.
       */
      public synchronized void waitForNextTaskEnd() throws InterruptedException
      {
         long completedBefore = completedRepetitions;
         while (completedBefore == completedRepetitions)
            waitForChange();
      }

      /**
       * Wait until the thread is paused.
       *
       * @throws InterruptedException If the waiting thread is interrupted.
       */
      public synchronized void waitForPause() throws InterruptedException
      {
         while (remainingRepetitions != 0)
            waitForChange();
      }

      /**
       * Get the remaining number of repetitions this thread plans to run.
       *
       * @return The remaining number of repetitions to execute.
       */
      public synchronized long getRemaining()
      {
         return remainingRepetitions;
      }

      /**
       * Get whether a task is currently executing.
       * @return Whether a task is currently executing.
       */
      public synchronized boolean isExecuting()
      {
         return executing;
      }

      /**
       * Get the total number of repetitions completed by this thread.
       *
       * @return The total number of repetitions completed by this thread.
       */
      public synchronized long getCompleted()
      {
         return completedRepetitions;
      }
   }
}
