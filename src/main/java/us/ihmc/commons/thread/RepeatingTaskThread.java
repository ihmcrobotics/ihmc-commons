package us.ihmc.commons.thread;

import us.ihmc.commons.Conversions;
import us.ihmc.commons.RunnableThatThrows;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.exception.ExceptionHandler;
import us.ihmc.commons.exception.ExceptionTools;

/**
 * A thread that repeats the execution of a single task. It can do this {@code n} times, continuously, or at a limited rate.
 * <p>
 * Unlike {@link Thread}, this class ensures that exceptions are caught and handled.
 * <p>
 *  The task to execute may be specified in two ways:
 *  <ol>
 *     <li> Providing a {@link RunnableThatThrows} during construction.
 *     <li> {@code @Override}ing the {@link #runTask()} method.
 *  </ol>
 * <p>
 * Upon construction, this thread will have zero scheduled task executions and the thread will not be started.
 * Execution can be started in two ways:
 *  <ol>
 *     <li> Call {@link #startRepeating()}
 *     <li> Call {@link #start()}, and {@link #setScheduled(long n)} or {@link #addScheduled(long n)} in any order.
 *  </ol>
 * <p>
 * The execution frequency may be limited with {@link #setFrequencyLimit(double)} and removed using {@link #removeFrequencyLimit()}.
 * The limit can be modified or removed at any time.
 * Note that this is only an upper limit. If the task overruns the corresponding period, the task will simply execute again
 * once its finished, resulting in a potentially jittery, slower frequency.
 * <p>
 * To support clear and concise initialization, {@link #setFrequencyLimit} returns {@code this}.
 * <p>
 * To allow the currently executing task to finish, if one is executing, and allow the thread to die,
 * use {@link #kill()} or {@link #blockingKill()}.
 */
public class RepeatingTaskThread extends Thread
{
   public static final int REPEAT_INDEFINITELY = -1;
   public static final double UNLIMITED_FREQUENCY = -1.0;

   private final RunnableThatThrows task;
   private final ExceptionHandler exceptionHandler;

   /**
    * Becomes {@code true} when the thread is started, and {@code false} when the thread is killed.
    * Once {@code false}, the task loop will allow the currently executing task (if any) to complete,
    * and the task loop is exited, allowing the thread to die.
    */
   private boolean running = false;
   
   /** Execution state of this thread */
   private final ExecutionState executionState = new ExecutionState();

   /** Throttler used to limit the execution frequency. */
   private final Throttler throttler = new Throttler();

   /** The period of the set frequency limit. A negative value indicates no limit. */
   private volatile double periodLowerLimit = UNLIMITED_FREQUENCY;

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
    * Limits the frequency of task execution.
    * <p>
    * This is only an upper limit. If the task overruns the corresponding period, the task will simply execute again
    * once its finished, resulting in a potentially jittery, slower frequency.
    * <p>
    * To remove the limit, use {@link #removeFrequencyLimit()} or set to {@link #UNLIMITED_FREQUENCY}.
    *
    * @param frequencyLimit The frequency limit or {@link #UNLIMITED_FREQUENCY}.
    * @return {@code this}, such that it can be used like a
    *       <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>.
    */
   public RepeatingTaskThread setFrequencyLimit(double frequencyLimit)
   {
      periodLowerLimit = Conversions.hertzToSeconds(frequencyLimit);
      return this;
   }

   /**
    * Removes the frequency limit.
    * <p>
    * Equivalent to {@code setFrequencyLimit(UNLIMITED_FREQUENCY)}.
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
    * Ensures the thread is started and schedules indefinitely repeating execution.
    */
   public void startRepeating()
   {
      if (!running)
         start();

      setRepeating(true);
   }

   /**
    * Clears the execution schedule. If the task is currently executing,
    * it is allowed to complete.
    */
   public void stopRepeating()
   {
      setRepeating(false);
   }

   /**
    * @param repeating If {@code true}, schedules indefinitely repeating execution.
    *                  If {@code false}, clears the execution schedule.
    */
   public void setRepeating(boolean repeating)
   {
      if (repeating)
         setScheduled(REPEAT_INDEFINITELY);
      else
         setScheduled(0);
   }

   /**
    * Sets the execution schedule to {@code n} executions.
    * <p>
    * If the task was not executing and {@code n > 0}, it will immediately begin executing.
    *
    * @param n The updated number of scheduled executions.
    */
   public void setScheduled(long n)
   {
      executionState.setScheduled(n);
   }

   /**
    * Schedules {@code n} more executions.
    * <p>
    * If the task was not executing and {@code n > 0}, it will immediately begin executing.
    * <p>
    * You may also subtract from the number of scheduled executions by passing in a negative number.
    * This method cannot cause the scheduled execution count to go below 0.
    * <p>
    * This method does not do anything if indefinitely repeating execution is scheduled.
    *
    * @param n If {@code n > 0}, the number of executions to schedule.
    *          If {@code n < 0}, the number of executions to unschedule.
    */
   public void addScheduled(long n)
   {
      executionState.addScheduled(n);
   }

   /**
    * @return The number of scheduled executions.
    */
   public long getScheduled()
   {
      return executionState.getScheduled();
   }

   /**
    * @return Whether a task is currently executing.
    */
   public boolean isExecuting()
   {
      return executionState.isExecuting();
   }

   /**
    * @return The total number of task executions completed by this thread.
    */
   public long getCompleted()
   {
      return executionState.getCompleted();
   }

   /**
    * Block until the next start of the task.
    */
   public void blockUntilNextTaskExecution()
   {
      synchronized (executionState)
      {
         do
         {
            executionState.waitForChange();
         } while (!executionState.isExecuting());
      }
   }

   /**
    * Block until the next completion of the task.
    */
   public void blockUntilNextTaskCompletion()
   {
      synchronized (executionState)
      {
         long completedBefore = executionState.getCompleted();
         while (completedBefore == executionState.getCompleted())
            executionState.waitForChange();
      }
   }

   /**
    * Block until there are no scheduled task executions, which may be immediately.
    */
   public void blockUntilNoScheduledTasks()
   {
      synchronized (executionState)
      {
         while (executionState.getScheduled() != 0 || executionState.isExecuting())
            executionState.waitForChange();
      }
   }

   /**
    * Allows the thread to die.
    * Any currently executing task will first be allowed to finish.
    * This class cannot be reused after this point.
    */
   public void kill()
   {
      running = false;
      synchronized (executionState)
      {
         executionState.notifyAll();
      }
   }

   /**
    * Allows the thread to die and blocks until it does.
    * Any currently executing task will first be allowed to finish.
    * This class cannot be reused after this point.
    * <p>
    * Same as calling {@link #kill()} then {@link #join()}.
    * Returns {@code true} if interrupted.
    */
   public boolean blockingKill()
   {
      kill();
      try
      {
         join();
      }
      catch (InterruptedException e)
      {
         return true;
      }

      return false;
   }

   /**
    * The method that is executed repeatedly.
    * <p>
    * You may {@code @Override} this method with the code to execute.
    * Otherwise, this method will execute the passed in {@link RunnableThatThrows}.
    *
    * @throws Throwable Any throwable that the executed code may throw.
    *       Will be handled by the {@link ExceptionHandler}
    *       (default: {@link DefaultExceptionHandler#MESSAGE_AND_STACKTRACE}).
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
    * DO NOT CALL THIS METHOD.
    * The existence of this method is a necessary evil for this class to extend Thread.
    */
   @Override
   public final void run()
   {
      while (running)
      {
         synchronized (executionState)
         {
            if (executionState.getScheduled() == 0L)
            {
               if (executionState.waitForChange())
                  interrupt(); // Maintain interrupted status so that runTask method can handle it

               continue;
            }
         }

         // If a period/frequency limit was set, wait until loop can run.
         if (periodLowerLimit > 0.0)
         {
            /*
             * This call must not swallow interrupts.
             * As of writing this comment (Nov, 2024), LockSupport.parkNanos() is used internally to block.
             * Although the throttler will block until the period has elapsed, the thread
             * remains interrupted.
             */
            throttler.waitAndRun(periodLowerLimit);
         }

         // Run the runTask method, and handle any exception it may throw.
         executionState.beforeTaskExecution();
         ExceptionTools.handle(this::runTask, exceptionHandler);
         executionState.afterTaskExecution();
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
      if (!running)
         return false;

      return executionState.getScheduled() != 0L;
   }

   /** The execution state of the RepeatingTaskThread. */
   private static class ExecutionState
   {
      /**
       * How many more times to execute the task.
       *
       * <ul>
       *    <li> n > 0 = Task will be executed n more times.
       *    <li> 0 = Task will not be executed again.
       *    <li> -1 = Task will be executed repeatedly and indefinitely.
       */
      private volatile long scheduledRepetitions = 0L;

      /** Whether the task is currently executing. */
      private volatile boolean executing = false;

      /** The total number of times the task has completed execution during the lifetime of this thread. */
      private volatile long completedRepetitions = 0L;

      private synchronized void beforeTaskExecution()
      {
         if (scheduledRepetitions > 0)
            --scheduledRepetitions;
         executing = true;
         notifyAll();
      }

      private synchronized void afterTaskExecution()
      {
         executing = false;
         ++completedRepetitions;
         notifyAll();
      }

      private synchronized void setScheduled(long repetitions)
      {
         scheduledRepetitions = repetitions;
         notifyAll();
      }

      private synchronized void addScheduled(long repetitions)
      {
         // If repeating indefinitely, do nothing
         if (scheduledRepetitions < 0L)
            return;

         // Add to the scheduled repetition counter, ensuring it doesn't become negative
         scheduledRepetitions = Math.max(scheduledRepetitions + repetitions, 0L);

         notifyAll();
      }

      /**
       * Wait until a change occurs to the thread's execution state.
       *
       * @return if was interrupted
       */
      private synchronized boolean waitForChange()
      {
         try
         {
            wait();
         }
         catch (InterruptedException e)
         {
            return true;
         }

         return false;
      }

      private long getScheduled()
      {
         return scheduledRepetitions;
      }

      private boolean isExecuting()
      {
         return executing;
      }

      private long getCompleted()
      {
         return completedRepetitions;
      }
   }
}
