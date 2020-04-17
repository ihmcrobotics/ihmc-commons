package us.ihmc.commons.thread;

import org.junit.jupiter.api.Test;
import us.ihmc.commons.Conversions;
import us.ihmc.commons.time.Stopwatch;
import us.ihmc.log.LogTools;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ThreadToolsTest
{
   @Test
   public void testTimeLimitScheduler()
   {
      final int ITERATIONS = 10;
      final double EPSILON = 40;

      TimeUnit timeUnit = TimeUnit.MILLISECONDS;
      long initialDelay = 0;
      long delay = 3;
      long timeLimit = 300;

      final Runnable runnable = () ->
      {
         //Do some calculation
         Math.sqrt(Math.PI);
      };

      for (int i = 0; i < ITERATIONS; i++)
      {
         long startTime = System.currentTimeMillis();
         ScheduledFuture<?> future = ThreadTools
               .scheduleWithFixeDelayAndTimeLimit(getClass().getSimpleName(), runnable, initialDelay, delay, timeUnit, timeLimit, true);
         while (!future.isDone())
         {
            // do nothing
         }
         long endTime = System.currentTimeMillis();
         assertEquals(timeLimit, endTime - startTime, EPSILON);
      }
   }

   @Test
   public void testTimeLimitSchedulerInterrupt()
   {
      TimeUnit timeUnit = TimeUnit.MILLISECONDS;
      long initialDelay = 0;
      long delay = 3;
      long timeLimit = 400;
      final AtomicInteger counter = new AtomicInteger();


      final Runnable runnable = () ->
      {
            counter.incrementAndGet();
            Stopwatch stopwatch1 = new Stopwatch().start();

            while (stopwatch1.lapElapsed() < 0.3)
            {

            }
      };

      Stopwatch stopwatch2 = new Stopwatch().start();
      ScheduledFuture<?> future = ThreadTools.scheduleWithFixeDelayAndTimeLimit(getClass().getSimpleName(),
                                                                                runnable,
                                                                                initialDelay,
                                                                                delay,
                                                                                timeUnit,
                                                                                timeLimit,
                                                                                true);
      while (!future.isDone())
      {
         // do nothing
      }

      double elapsedMilliseconds = Conversions.secondsToMilliseconds(stopwatch2.totalElapsed());
      LogTools.info("elapsedMilliseconds = " + elapsedMilliseconds);
      assertEquals(300, elapsedMilliseconds, 10);
      assertEquals(2, counter.get());
   }

   @Test
   public void testSingleExecution()
   {
      TimeUnit timeUnit = TimeUnit.MILLISECONDS;
      long delay = 100;

      final AtomicInteger counter = new AtomicInteger();

      final Runnable runnable = () -> counter.incrementAndGet();

      ScheduledFuture<?> future = ThreadTools.scheduleSingleExecution(getClass().getSimpleName(), runnable, delay, timeUnit);

      Stopwatch stopwatch = new Stopwatch().start();
      while (!future.isDone())
      {
         // do nothing
      }

      double elapsedMilliseconds = Conversions.secondsToMilliseconds(stopwatch.totalElapsed());
      LogTools.info("elapsedMilliseconds = " + elapsedMilliseconds);

      assertEquals(100, elapsedMilliseconds, 10);
      assertEquals(1, counter.get());
   }

   @Test
   public void testIterationLimitScheduler()
   {
      TimeUnit timeUnit = TimeUnit.MILLISECONDS;
      long initialDelay = 0;
      long delay = 10;
      final int iterations = 10;

      final AtomicInteger counter = new AtomicInteger();

      final Runnable runnable = () -> counter.incrementAndGet();

      ScheduledFuture<?> future = ThreadTools
            .scheduleWithFixedDelayAndIterationLimit(getClass().getSimpleName(), runnable, initialDelay, delay, timeUnit, iterations);

      while (!future.isDone())
      {
         // do nothing
      }

      assertEquals(iterations, counter.get());
   }

   @Test
   public void testExecuteWithTimeout()
   {
      final StateHolder holder = new StateHolder();
      for (int i = 0; i < 10; i++)
      {
         holder.state = State.DIDNT_RUN;
         ThreadTools.executeWithTimeout("timeoutTest1", new Runnable()
         {
            @Override
            public void run()
            {
               holder.state = State.TIMED_OUT;

               ThreadTools.sleep(10);

               holder.state = State.RAN_WITHOUT_TIMING_OUT;
            }
         }, 5, TimeUnit.MILLISECONDS);
         assertFalse(holder.state.equals(State.DIDNT_RUN), "Didn't run. Should timeout.");
         assertTrue(holder.state.equals(State.TIMED_OUT), "Did not timeout.");

         holder.state = State.DIDNT_RUN;
         ThreadTools.executeWithTimeout("timeoutTest2", new Runnable()
         {
            @Override
            public void run()
            {
               holder.state = State.TIMED_OUT;

               ThreadTools.sleep(5);

               holder.state = State.RAN_WITHOUT_TIMING_OUT;
            }
         }, 10, TimeUnit.MILLISECONDS);
         assertFalse(holder.state.equals(State.DIDNT_RUN), "Didn't run. Shouldn't timeout.");
         assertTrue(holder.state.equals(State.RAN_WITHOUT_TIMING_OUT), "Timed out early.");
      }
   }

   @Test
   public void testThreadSleepEvenWhenInterrupted()
   {
      final long ONE_MILLION = 1000000;
      long millisecondsToSleep = 1100;
      int additionalNanosecondsToSleep = 500000;

      long totalNanosecondsToSleep = millisecondsToSleep * ONE_MILLION + additionalNanosecondsToSleep;

      SleepAndVerifyDespiteWakingUpRunnable runnable = new SleepAndVerifyDespiteWakingUpRunnable(millisecondsToSleep, additionalNanosecondsToSleep);

      int numberOfTimesToTest = 5;
      for (int i = 0; i < numberOfTimesToTest; i++)
      {
         Thread thread = new Thread(runnable);
         thread.start();

         while (!runnable.isDoneSleeping())
         {
            // Here we interrupt the thread to make sure that it sleeps for the total amount of time requested.
            thread.interrupt();

            try
            {
               Thread.sleep(millisecondsToSleep / 10);
            }
            catch (InterruptedException e)
            {
            }
         }

         long timeSleptInNanoseconds = runnable.getTimeSleptNanonseconds();
         long timeOverSleptInNanoseconds = timeSleptInNanoseconds - totalNanosecondsToSleep;

         // Check to make sure slept at least the amount specified. Method guarantees it sleeps at least or more than requested.
         assertTrue(timeOverSleptInNanoseconds > 0, "timeSlept = " + timeSleptInNanoseconds + ", totalNanosecondsToSleep = " + totalNanosecondsToSleep + " timeOverSleptInNanoseconds = "
                                   + timeOverSleptInNanoseconds);

         // Check to make sure didn't over sleep by more than 100 milliseconds, which seems reasonable on most operating systems.
         assertTrue(timeOverSleptInNanoseconds < 100 * ONE_MILLION, "timeSlept = " + timeSleptInNanoseconds + ", millisecondsToSleep = " + millisecondsToSleep);

         // Now make sure it doesn't get interrupted if we don't interrupt it...
         runnable = new SleepAndVerifyDespiteWakingUpRunnable(millisecondsToSleep, additionalNanosecondsToSleep);

         thread = new Thread(runnable);
         thread.start();

         while (!runnable.isDoneSleeping())
         {
            try
            {
               Thread.sleep(millisecondsToSleep / 10);
            }
            catch (InterruptedException e)
            {
            }
         }
      }
   }

   private class SleepAndVerifyDespiteWakingUpRunnable implements Runnable
   {
      private long millisecondsToSleep;
      private int additonalNanosecondsToSleep;
      private boolean isDoneSleeping;
      private long timeSleptNanosecondsMeasuredExternally;

      public SleepAndVerifyDespiteWakingUpRunnable(long millisecondsToSleep, int additonalNanosecondsToSleep)
      {
         this.millisecondsToSleep = millisecondsToSleep;
         this.additonalNanosecondsToSleep = additonalNanosecondsToSleep;
      }

      public boolean isDoneSleeping()
      {
         return isDoneSleeping;
      }

      public long getTimeSleptNanonseconds()
      {
         return timeSleptNanosecondsMeasuredExternally;
      }

      @Override
      public void run()
      {
         long startTime = System.nanoTime();
         long timeSleptMeasuredFromMethod = ThreadTools.sleep(millisecondsToSleep, additonalNanosecondsToSleep);

         long endTime = System.nanoTime();
         timeSleptNanosecondsMeasuredExternally = endTime - startTime;

         if (timeSleptNanosecondsMeasuredExternally < timeSleptMeasuredFromMethod)
         {
            throw new AssertionError(
                  "Huh: timeSleptNanosecondsMeasuredExternally = " + timeSleptNanosecondsMeasuredExternally + ", timeSleptMeasuredFromMethod = "
                        + timeSleptMeasuredFromMethod);
         }
         isDoneSleeping = true;
      }
   }

   private enum State
   {
      DIDNT_RUN, TIMED_OUT, RAN_WITHOUT_TIMING_OUT;
   }

   private class StateHolder
   {
      public State state = State.DIDNT_RUN;
   }
}
