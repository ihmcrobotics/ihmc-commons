package us.ihmc.commons.thread;

import us.ihmc.commons.Conversions;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.exception.ExceptionHandler;
import us.ihmc.commons.exception.ExceptionTools;
import us.ihmc.commons.time.Stopwatch;
import us.ihmc.log.LogTools;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * This class provides convenience features on top of
 * {@link Executors java.util.concurrent.Executors},
 * {@link Thread java.lang.Thread}
 * </p>
 *
 * <p>
 * It focueses on these main objectives:
 * <ul>
 *    <li>Advocating and enforcing that threads have useful names</li>
 *    <li>Providing API that accepts seconds as a double instead of using TimeUnit</li>
 *    <li>Providing a sleep method that ensures a minimum bound on duration</li>
 *    <li>Exception handling for convenience or accepting ExceptionHandler to avoid try catch blocks everywhere</li>
 *    <li>Advanced task scheduling with iteration and time limits</li>
 * </ul>
 * </p>
 */
public class ThreadTools
{
   private static final AtomicInteger poolNumber = new AtomicInteger(1);

   public static final int REASONABLE_WAITING_SLEEP_DURATION_MS = 10;

   private static final long ONE_MILLION = 1000000;

   /**
    * Causes the currently executing thread to sleep (temporarily cease execution) for the specified number of seconds,
    * subject to the precision and accuracy of system timers and schedulers. If the sleep is interrupted with a InterruptedException,
    * it will ignore the interruption, see how long it has slept so far, and go back to sleep for the remaining time.
    * When it is fully done sleeping, it will interrupt its Thread again if it was interrupted at all during sleeping.
    *
    * @param secondsToSleep The time to sleep in seconds. The Thread should sleep this long, even if interrupted.
    * @return actual nanoseconds slept
    */
   public static long sleepSeconds(double secondsToSleep)
   {
      return sleep((long) (secondsToSleep * 1000));
   }

   /**
    * Causes the currently executing thread to sleep (temporarily cease execution) for the specified number of milliseconds,
    * subject to the precision and accuracy of system timers and schedulers. If the sleep is interrupted with a InterruptedException,
    * it will ignore the interruption, see how long it has slept so far, and go back to sleep for the remaining time.
    * When it is fully done sleeping, it will interrupt its Thread again if it was interrupted at all during sleeping.
    *
    * @param millisecondsToSleep The time to sleep in milliseconds. The Thread should sleep this long, even if interrupted.
    * @return actual nanoseconds slept
    */
   public static long sleep(long millisecondsToSleep)
   {
      return sleep(millisecondsToSleep, 0);
   }

   /**
    * Causes the currently executing thread to sleep (temporarily cease execution) for the specified number of milliseconds
    * plus the specified number of nanoseconds, subject to the precision and accuracy of system timers and schedulers.
    * If the sleep is interrupted with a InterruptedException,
    * it will ignore the interruption, see how long it has slept so far, and go back to sleep for the remaining time.
    * It is guaranteed to sleep for at least the requested amount of time. May sleep a little more.
    *
    * @param millisecondsToSleep The time to sleep in milliseconds. The Thread should sleep this long, even if interrupted.
    * @param additionalNanosecondsToSleep 0-999999 additional nanoseconds to sleep
    * @return actual nanoseconds slept
    */
   public static long sleep(long millisecondsToSleep, int additionalNanosecondsToSleep)
   {
      long startTimeNanos = System.nanoTime();
      long desiredSleepNanos = millisecondsToSleep * ONE_MILLION + additionalNanosecondsToSleep;
      boolean doneSleeping = false;
      long nanosSleptSoFar = 0L;

      while (!doneSleeping)
      {
         try
         {
            Thread.sleep(millisecondsToSleep, additionalNanosecondsToSleep);
         }
         catch (InterruptedException ex)
         {
         }

         nanosSleptSoFar = System.nanoTime() - startTimeNanos;

         if (nanosSleptSoFar >= desiredSleepNanos)
         {
            doneSleeping = true;
         }
         else
         {
            long nanosRemaining = desiredSleepNanos - nanosSleptSoFar;
            millisecondsToSleep = nanosRemaining / ONE_MILLION;
            additionalNanosecondsToSleep = (int) (nanosRemaining - (millisecondsToSleep * ONE_MILLION));
         }
      }

      return nanosSleptSoFar;
   }

   /**
    * Causes this Thread to continuously sleep, ignoring any interruptions.
    *
    * It is recommended to use {@link #join} instead.
    */
   public static void sleepForever()
   {
      while (true)
      {
         ThreadTools.sleep(1000);
      }
   }

   /**
    * Join from current thread, printing stack trace if interrupted.
    */
   public static void join()
   {
      join(DefaultExceptionHandler.PRINT_STACKTRACE);
   }

   /**
    * Joins from the current thread, handling exception. {@link Thread#currentThread() Thread.currentThread().join()}
    */
   public static void join(ExceptionHandler exceptionHandler)
   {
      ExceptionTools.handle(() -> Thread.currentThread().join(), exceptionHandler);
   }

   /**
    * Starts a user thread.
    * To start a daemon thread, see {@linkplain #startAsDaemon startAsDaemon}
    */
   public static Thread startAThread(Runnable runnable, String threadName)
   {
      Thread newThread = new Thread(runnable, threadName);
      newThread.start();
      return newThread;
   }

   /**
    * Starts a daemon thread.
    * The Java Virtual Machine exits when the only threads running are all daemon threads.
    */
   public static Thread startAsDaemon(Runnable daemonThreadRunnable, String threadName)
   {
      Thread daemonThread = new Thread(daemonThreadRunnable, threadName);
      daemonThread.setDaemon(true);
      daemonThread.start();
      return daemonThread;
   }

   public static void waitUntilNextMultipleOf(long waitMultipleMS) throws InterruptedException
   {
      waitUntilNextMultipleOf(waitMultipleMS, 0);
   }

   public static void waitUntilNextMultipleOf(long waitMultipleMS, long moduloOffset) throws InterruptedException
   {
      long startTime = System.currentTimeMillis();
      long numberOfMultiplesThusFar = (startTime - moduloOffset) / waitMultipleMS;
      long endTime = (numberOfMultiplesThusFar + 1) * waitMultipleMS + moduloOffset;
      waitUntil(endTime);
   }

   public static void waitUntil(long endTime) throws InterruptedException
   {
      while (true)
      {
         if (endTime <= System.currentTimeMillis())
            break;
         Thread.sleep(REASONABLE_WAITING_SLEEP_DURATION_MS);
      }
   }

   /**
    * @deprecated Use {@link #createNamedThreadFactory} instead
    */
   public static ThreadFactory getNamedThreadFactory(String name)
   {
      return createNamedThreadFactory(name);
   }

   /**
    * Thread factory that creates non-daemon threads with normal priority
    * with the naming scheme "name-pool-1-thread-1", "name-pool-1-thread-2", ...
    *
    * @param prefix useful name
    * @return thread factory
    */
   public static ThreadFactory createNamedThreadFactory(String prefix)
   {
      boolean includePoolInName = true;
      boolean includeThreadNumberInName = true;
      boolean daemon = false;
      return createNamedThreadFactory(prefix, includePoolInName, includeThreadNumberInName, daemon, Thread.NORM_PRIORITY);
   }

   /**
    * Thread factory that creates daemon threads with normal priority
    * with the naming scheme "name-pool-1-thread-1", "name-pool-1-thread-2", ...
    *
    * @param prefix useful name
    * @return thread factory
    */
   public static ThreadFactory createNamedDaemonThreadFactory(String prefix)
   {
      boolean includePoolInName = true;
      boolean includeThreadNumberInName = true;
      boolean daemon = true;
      return createNamedThreadFactory(prefix, includePoolInName, includeThreadNumberInName, daemon, Thread.NORM_PRIORITY);
   }

   /**
    * Thread factory that creates threads identical to {@link java.util.concurrent.Executors}.DefaultThreadFactory
    * except that a useful name is prepended.
    *
    * @param prefix useful name to identify the purpose of threads
    * @param includePoolInName include "-pool-N" in the thread name
    * @param includeThreadNumberInName include "-thread-M" in the thread name
    * @param daemon set threads to daemon
    * @param priority set priority of new threads
    * @return thread factory
    */
   public static ThreadFactory createNamedThreadFactory(final String prefix,
                                                        boolean includePoolInName,
                                                        boolean includeThreadNumberInName,
                                                        boolean daemon,
                                                        int priority)
   {
      return new ThreadFactory()
      {
         private final AtomicInteger threadNumber = new AtomicInteger(1);
         private final ThreadGroup group;
         {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            poolNumber.getAndIncrement();
         }

         @Override
         public Thread newThread(Runnable runnable)
         {
            threadNumber.getAndIncrement();
            String threadName = prefix;
            if (includePoolInName)
            {
               threadName += "-pool-" + poolNumber.get();
            }
            if (includeThreadNumberInName)
            {
               threadName += "-thread-" + threadNumber.get();
            }
            Thread newThread = new Thread(group, runnable, threadName);
            newThread.setDaemon(daemon);
            newThread.setPriority(priority);
            return newThread;
         }
      };
   }

   /**
    * Create a single thread executor with non-daemon threads and normal priority.
    *
    * @see Executors#newSingleThreadExecutor(ThreadFactory)
    * @param prefix
    * @return
    */
   public static Executor newSingleThreadExecutor(String prefix)
   {
      return Executors.newSingleThreadExecutor(createNamedThreadFactory(prefix));
   }

   /**
    * Create a single thread executor with non-daemon threads and normal priority.
    *
    * @see Executors#newSingleThreadExecutor(ThreadFactory)
    * @param prefix
    * @return
    */
   public static ScheduledExecutorService newSingleThreadScheduledExecutor(String prefix)
   {
      return Executors.newSingleThreadScheduledExecutor(createNamedThreadFactory(prefix));
   }

   /**
    * Create a single thread executor in which all created threads are daemon thread, meaning that they will
    * be terminated and not cause the application to hang when the main thread has been terminated.
    *
    * @see Executors#newSingleThreadExecutor(ThreadFactory)
    * @param prefix
    * @return
    */
   public static Executor newSingleDaemonThreadExecutor(String prefix)
   {
      return Executors.newSingleThreadExecutor(createNamedDaemonThreadFactory(prefix));
   }

   /**
    * Create a single thread executor in which all created threads are daemon thread, meaning that they will
    * be terminated and not cause the application to hang when the main thread has been terminated.
    *
    * @see Executors#newSingleThreadScheduledExecutor(ThreadFactory)
    * @param prefix
    * @return
    */
   public static ScheduledExecutorService newSingleDaemonThreadScheduledExecutor(String prefix)
   {
      return Executors.newSingleThreadScheduledExecutor(createNamedDaemonThreadFactory(prefix));
   }

   public static String getBaseClassName()
   {
      StackTraceElement[] stack = Thread.currentThread().getStackTrace();
      String className = stack[stack.length - 1].getClassName();
      return className;
   }

   public static String getBaseSimpleClassName()
   {
      String baseClassName = getBaseClassName();
      int lastDotIndex = baseClassName.lastIndexOf('.');
      String simpleClassName = baseClassName.substring(lastDotIndex + 1);
      return simpleClassName;
   }

   public static void interruptLiveThreadsExceptThisOneContaining(String stringToContain)
   {
      Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
      Set<Thread> threadSet = allStackTraces.keySet();

      for (Thread thread : threadSet)
      {
         if (thread.isAlive() && thread != Thread.currentThread())
         {
            if (thread.getName().contains(stringToContain))
            {
               //               System.out.println("Interrupting thread " + thread.getName());
               thread.interrupt();
            }
         }
      }
   }

   public static void interruptAllAliveThreadsExceptThisOne()
   {
      Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
      Set<Thread> threadSet = allStackTraces.keySet();

      for (Thread thread : threadSet)
      {
         if (thread.isAlive() && thread != Thread.currentThread())
         {
            thread.interrupt();
         }
      }
   }

   /**
    * This method executes a single tasks an blocks until completion with time limit.
    * If the thread runs past the time limit, it is interrupted and the method returns.
    *
    * @param threadName useful name
    * @param runnable
    * @param timeLimit time before interruption
    * @param timeUnit time limit units
    * @return the executor object that was created to execute the task
    */
   public static ExecutorService executeWithTimeout(String threadName, Runnable runnable, long timeLimit, TimeUnit timeUnit)
   {
      ExecutorService executor = Executors.newSingleThreadExecutor(createNamedThreadFactory(threadName));
      executor.execute(runnable);
      executor.shutdown();
      try
      {
         executor.awaitTermination(timeLimit, timeUnit);
      }
      catch (InterruptedException e)
      {
         e.printStackTrace();
      }

      return executor;
   }

   /**
    * @deprecated Use {@link #scheduleWithFixeDelayAndTimeLimit} instead. This method has an ambiguous end criteria.
    */
   public static ScheduledFuture<?> scheduleWithFixeDelayAndTimeLimit(String threadName,
                                                                      final Runnable runnable,
                                                                      long initialDelay,
                                                                      long delay,
                                                                      TimeUnit timeUnit,
                                                                      final long timeLimit)
   {
      return scheduleWithFixeDelayAndTimeLimit(threadName, runnable, initialDelay, delay, timeUnit, timeLimit, true);
   }

   /**
    * Schedules a periodic task with {@link ScheduledExecutorService#scheduleWithFixedDelay} but adds a time limit.
    * The user may choose whether to interrupt when the time expires or wait for the last run to complete (if currently
    * executing when the time expires)
    *
    * @param threadName useful name
    * @param runnable task
    * @param initialDelay
    * @param delay
    * @param timeUnit
    * @param timeLimit
    * @param interruptAtTimeLimit whether to interrupt the runnable at the time limit, if false, waits to run to complete then cancels
    *                             setting to true will require an extra thread
    */
   public static ScheduledFuture<?> scheduleWithFixeDelayAndTimeLimit(String threadName,
                                                                      final Runnable runnable,
                                                                      long initialDelay,
                                                                      long delay,
                                                                      TimeUnit timeUnit,
                                                                      final long timeLimit,
                                                                      boolean interruptAtTimeLimit)
   {

      ScheduledFuture<?> futureToReturn;

      if (interruptAtTimeLimit)
      {
         ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, getNamedThreadFactory(threadName));
         futureToReturn = scheduler.scheduleWithFixedDelay(runnable, initialDelay, delay, timeUnit);
         scheduler.schedule(() ->
         {
            boolean cancel = futureToReturn.cancel(true);
            LogTools.info("Cancel: {}", cancel);
            return cancel;
         }, timeLimit, timeUnit);
      }
      else
      {
         ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(createNamedThreadFactory(threadName));
         AtomicReference<ScheduledFuture<?>> handleHolder = new AtomicReference<>();
         double timeLimitSeconds = Conversions.nanosecondsToSeconds(timeUnit.toNanos(timeLimit));
         Stopwatch stopwatch = new Stopwatch().start();
         handleHolder.set(scheduler.scheduleWithFixedDelay(() ->
         {
            if (stopwatch.lapElapsed() < timeLimitSeconds)
            {
               runnable.run();
            }

            // runnable might take some time. avoid waiting another delay to cancel
            if (stopwatch.lapElapsed() >= timeLimitSeconds)
            {
               ScheduledFuture<?> scheduledFuture = handleHolder.get();
               if (scheduledFuture != null)
               {
                  scheduledFuture.cancel(true);
               }
            }
         }, initialDelay, delay, timeUnit));
         futureToReturn = handleHolder.get();
      }

      return futureToReturn;
   }

   /**
    * Schedule a single task to start after delay.
    * 
    * @param threadName useful name
    * @param runnable task
    * @param delay in seconds
    * @return executor that the task is scheduled with
    */
   public static ScheduledFuture<?> scheduleSingleExecution(String threadName, Runnable runnable, double delay)
   {
      return scheduleSingleExecution(threadName, runnable, Conversions.secondsToNanoseconds(delay), TimeUnit.NANOSECONDS);
   }

   /**
    * Schedule a single task to start after a delay.
    * 
    * @param threadName useful name
    * @param runnable 
    * @param delay
    * @param timeUnit
    * @return future that the task is scheduled with
    */
   public static ScheduledFuture<?> scheduleSingleExecution(String threadName, Runnable runnable, long delay, TimeUnit timeUnit)
   {
      ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(createNamedThreadFactory(threadName));
      return executor.schedule(runnable, delay, timeUnit);
   }

   public static ScheduledFuture<?> scheduleWithFixedDelayAndIterationLimit(String threadName,
                                                                            Runnable runnable,
                                                                            double initialDelay,
                                                                            double delay,
                                                                            int iterations)
   {
      long initialDelayNanos = Conversions.secondsToNanoseconds(initialDelay);
      long delayNanos = Conversions.secondsToNanoseconds(delay);
      return scheduleWithFixedDelayAndIterationLimit(threadName, runnable, initialDelayNanos, delayNanos, TimeUnit.NANOSECONDS, iterations);
   }

   /**
    * Schedule a periodic task with {@link ScheduledExecutorService#scheduleWithFixedDelay} but with an added iteration limit
    * that when reached, cancels further execution.
    * 
    * @param threadName
    * @param runnable
    * @param initialDelay
    * @param delay
    * @param timeUnit
    * @param iterations
    * @return
    */
   public static ScheduledFuture<?> scheduleWithFixedDelayAndIterationLimit(String threadName,
                                                                            Runnable runnable,
                                                                            long initialDelay,
                                                                            long delay,
                                                                            TimeUnit timeUnit,
                                                                            int iterations)
   {
      AtomicInteger counter = new AtomicInteger(0);
      AtomicReference<ScheduledFuture<?>> handleHolder = new AtomicReference<>();

      ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(createNamedThreadFactory(threadName));

      handleHolder.set(scheduler.scheduleWithFixedDelay(() ->
      {
         if (counter.get() < iterations)
         {
            runnable.run();
         }

         counter.incrementAndGet();

         if (counter.get() >= iterations)
         {
            ScheduledFuture<?> scheduledFuture = handleHolder.get();
            if (scheduledFuture != null)
            {
               scheduledFuture.cancel(true);
            }
         }
      }, initialDelay, delay, timeUnit));

      return handleHolder.get();
   }
}
