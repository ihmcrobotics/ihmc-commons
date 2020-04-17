package us.ihmc.commons.thread;

import us.ihmc.commons.Conversions;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.exception.ExceptionTools;
import us.ihmc.commons.time.Stopwatch;
import us.ihmc.log.LogTools;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ThreadTools
{
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
    */
   public static void sleepForever()
   {
      while (true)
      {
         ThreadTools.sleep(1000);
      }
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
    * with the naming scheme "name-thread-1", "name-thread-2", ...
    *
    * @param name
    * @return thread factory
    */
   public static ThreadFactory createNamedThreadFactory(String name)
   {
      return createNamedThreadFactory(name + "-thread-", false, Thread.NORM_PRIORITY);
   }

   /**
    * Thread factory that creates daemon threads with normal priority
    * with the naming scheme "name-thread-1", "name-thread-2", ...
    *
    * @param name
    * @return thread factory
    */
   public static ThreadFactory createNamedDaemonThreadFactory(String name)
   {
      return createNamedThreadFactory(name + "-thread-", true, Thread.NORM_PRIORITY);
   }

   /**
    * Thread factory that creates threads
    * with the naming scheme "prefix-1", "prefix-2", ...
    *
    * @param prefix to use in naming
    * @param daemon set threads to daemon
    * @param priority set priority of new threads
    * @return thread factory
    */
   public static ThreadFactory createNamedThreadFactory(String prefix, boolean daemon, int priority)
   {
      return new ThreadFactory()
      {
         private final AtomicInteger threadNumber = new AtomicInteger(1);

         @Override
         public Thread newThread(Runnable runnable)
         {
            Thread newThread = new Thread(runnable, prefix + threadNumber.getAndIncrement());
            newThread.setDaemon(daemon);
            newThread.setPriority(priority);
            return newThread;
         }
      };
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

   public static ExecutorService executeWithTimeout(String threadName, Runnable runnable, long timeout, TimeUnit timeUnit)
   {
      ExecutorService executor = Executors.newSingleThreadExecutor(getNamedThreadFactory(threadName));
      executor.execute(runnable);
      executor.shutdown();
      try
      {
         executor.awaitTermination(timeout, timeUnit);
      }
      catch (InterruptedException e)
      {
         e.printStackTrace();
      }

      return executor;
   }

   @Deprecated // Does not specify hardness of time limit
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
      ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(getNamedThreadFactory(threadName));

      ScheduledFuture<?> futureToReturn;

      if (interruptAtTimeLimit)
      {
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

   public static ScheduledFuture<?> scheduleSingleExecution(String threadName, Runnable runnable, double delay)
   {
      return scheduleSingleExecution(threadName, runnable, Conversions.secondsToNanoseconds(delay), TimeUnit.NANOSECONDS);
//      return scheduleSingleExecution(threadName, runnable, Conversions.secondsToNanoseconds(delay), TimeUnit.NANOSECONDS);
   }

   public static ScheduledFuture<?> scheduleSingleExecution(String threadName, Runnable runnable, long delay, TimeUnit timeUnit)
   {
      int willBeIgnored = 100;
      return scheduleWithFixedDelayAndIterationLimit(threadName, runnable, delay, willBeIgnored, timeUnit, 1);
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

   public static ScheduledFuture<?> scheduleWithFixedDelayAndIterationLimit(String threadName,
                                                                            Runnable runnable,
                                                                            long initialDelay,
                                                                            long delay,
                                                                            TimeUnit timeUnit,
                                                                            int iterations)
   {
      AtomicInteger counter = new AtomicInteger(0);
      AtomicReference<ScheduledFuture<?>> handleHolder = new AtomicReference<>();

      ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, getNamedThreadFactory(threadName));

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

   /**
    * Create a single thread executor in which all created threads are daemon thread, meaning that they will
    * be terminated and not cause the application to hang when the main thread has been terminated.
    *
    * @see Executors#newSingleThreadExecutor(ThreadFactory)
    * @param name
    * @return
    */
   public static Executor newSingleDaemonThreadExecutor(String name)
   {
      return Executors.newSingleThreadExecutor(DaemonThreadFactory.getNamedDaemonThreadFactory(name));
   }

   /**
    * Create a single thread executor in which all created threads are daemon thread, meaning that they will
    * be terminated and not cause the application to hang when the main thread has been terminated.
    *
    * @see Executors#newSingleThreadScheduledExecutor(ThreadFactory)
    * @param name
    * @return
    */
   public static ScheduledExecutorService newSingleDaemonThreadScheduledExecutor(String name)
   {
      return Executors.newSingleThreadScheduledExecutor(DaemonThreadFactory.getNamedDaemonThreadFactory(name));
   }

   /**
    * Join from current thread, printing stack trace if interrupted.
    */
   public static void join()
   {
      ExceptionTools.handle(() -> Thread.currentThread().join(), DefaultExceptionHandler.PRINT_STACKTRACE);
   }
}
