package us.ihmc.commons.thread;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.jupiter.api.Test;
import us.ihmc.commons.Conversions;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.exception.ExceptionTools;
import us.ihmc.commons.time.Stopwatch;
import us.ihmc.log.LogTools;

import java.util.concurrent.ScheduledExecutorService;
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
      long timeLimit = 300;
      final AtomicInteger counter = new AtomicInteger();


      final Runnable runnable = () ->
      {
         counter.incrementAndGet();

         try
         {
            Thread.sleep(200);
         }
         catch (InterruptedException e)
         {
            LogTools.info("Interrupted!");
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
      assertEquals(300, elapsedMilliseconds, 30);
      assertEquals(2, counter.get());
   }

   @Test
   public void testTimeLimitSchedulerGraceful()
   {
      TimeUnit timeUnit = TimeUnit.MILLISECONDS;
      long initialDelay = 0;
      long delay = 3;
      long timeLimit = 300;
      final AtomicInteger counter = new AtomicInteger();


      final Runnable runnable = () ->
      {
         counter.incrementAndGet();

         try
         {
            Thread.sleep(200);
         }
         catch (InterruptedException e)
         {
            LogTools.info("Interrupted!");
         }
      };

      Stopwatch stopwatch2 = new Stopwatch().start();
      ScheduledFuture<?> future = ThreadTools.scheduleWithFixeDelayAndTimeLimit(getClass().getSimpleName(),
                                                                                runnable,
                                                                                initialDelay,
                                                                                delay,
                                                                                timeUnit,
                                                                                timeLimit,
                                                                                false);
      while (!future.isDone())
      {
         // do nothing
      }

      double elapsedMilliseconds = Conversions.secondsToMilliseconds(stopwatch2.totalElapsed());
      LogTools.info("elapsedMilliseconds = " + elapsedMilliseconds);
      assertEquals(400, elapsedMilliseconds, 30);
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

   @Test
   public void testParkInterrupt()
   {
      MutableBoolean interruptedBefore = new MutableBoolean(false);
      MutableBoolean interruptedAfter = new MutableBoolean(false);
      MutableBoolean interruptedAfterClear = new MutableBoolean(false);

      LogTools.info("Test park interrupted early return");
      Thread thread = new Thread("Test")
      {
         @Override
         public void run()
         {
            interruptedBefore.setValue(isInterrupted());
            ThreadTools.park(1.0);
            interruptedAfter.setValue(interrupted());
            interruptedAfterClear.setValue(isInterrupted());
         }
      };
      Stopwatch stopwatch = new Stopwatch().start();
      thread.start();
      ThreadTools.park(0.25);
      thread.interrupt();
      assertDoesNotThrow(() -> thread.join());
      double elapsed = stopwatch.totalElapsed();

      assertTrue(elapsed > 0.2 && elapsed < 0.3);

      assertFalse(interruptedBefore.booleanValue());
      assertTrue(interruptedAfter.booleanValue());
      assertFalse(interruptedAfterClear.booleanValue());

      LogTools.info("Test park immediate return if already interrupted");
      Thread thread2 = new Thread("Test2")
      {
         @Override
         public void run()
         {
            interruptedBefore.setValue(isInterrupted());
            ThreadTools.park(1.0);
            ThreadTools.park(1.0); // Test that the 2nd one also returns immediately
            interruptedAfter.setValue(interrupted());
            interruptedAfterClear.setValue(isInterrupted());
         }
      };
      thread2.interrupt();

      stopwatch = new Stopwatch().start();
      thread2.start();
      assertDoesNotThrow(() -> thread2.join());
      elapsed = stopwatch.totalElapsed();

      assertTrue(elapsed < 0.1);

      assertTrue(interruptedBefore.booleanValue());
      assertTrue(interruptedAfter.booleanValue());
      assertFalse(interruptedAfterClear.booleanValue());

      LogTools.info("Test parkAtLeast interrupted early return");
      Thread thread3 = new Thread("Test")
      {
         @Override
         public void run()
         {
            interruptedBefore.setValue(isInterrupted());
            ThreadTools.parkAtLeast(1.0);
            interruptedAfter.setValue(interrupted());
            interruptedAfterClear.setValue(isInterrupted());
         }
      };
      stopwatch = new Stopwatch().start();
      thread3.start();
      ThreadTools.park(0.25);
      thread3.interrupt();
      assertDoesNotThrow(() -> thread3.join());
      elapsed = stopwatch.totalElapsed();

      assertTrue(elapsed > 0.2 && elapsed < 0.3);

      assertFalse(interruptedBefore.booleanValue());
      assertTrue(interruptedAfter.booleanValue());
      assertFalse(interruptedAfterClear.booleanValue());

      LogTools.info("Test parkAtLeast when interrupted");
      Thread thread4 = new Thread("Test2")
      {
         @Override
         public void run()
         {
            interruptedBefore.setValue(isInterrupted());
            ThreadTools.parkAtLeast(1.0);
            ThreadTools.parkAtLeast(1.0); // Test that the 2nd one also returns immediately
            interruptedAfter.setValue(interrupted());
            interruptedAfterClear.setValue(isInterrupted());
         }
      };
      thread4.interrupt();

      stopwatch = new Stopwatch().start();
      thread4.start();
      assertDoesNotThrow(() -> thread4.join());
      elapsed = stopwatch.totalElapsed();

      assertTrue(elapsed < 0.1);

      assertTrue(interruptedBefore.booleanValue());
      assertTrue(interruptedAfter.booleanValue());
      assertFalse(interruptedAfterClear.booleanValue());
   }

   @Test
   public void testParkAtLeast()
   {
      assertTrue(conductParkTest(0.0000000000001, false));
      assertTrue(conductParkTest(0.5e-9, false));
      assertTrue(conductParkTest(1e-9, false));
      assertTrue(conductParkTest(0.1, false));
      assertTrue(conductParkTest(0.0001, false));
      assertTrue(conductParkTest(0.0000000005, false));
      assertTrue(conductParkTest(1.1, false));
      assertTrue(conductParkTest(2.0, false));

      assertTrue(conductParkTest(0.0000000000001, true));
      assertTrue(conductParkTest(0.5e-9, true));
      assertTrue(conductParkTest(1e-9, true));
      assertTrue(conductParkTest(0.1, true));
      assertTrue(conductParkTest(0.0001, true));
      assertTrue(conductParkTest(0.0000000005, true));
      assertTrue(conductParkTest(1.1, true));
      assertTrue(conductParkTest(2.0, true));
   }

   private boolean conductParkTest(double sleepDuration, boolean atLeast)
   {
      double before = Conversions.nanosecondsToSeconds(System.nanoTime());

      if (atLeast)
         ThreadTools.parkAtLeast(sleepDuration);
      else
         ThreadTools.park(sleepDuration);

      double after = Conversions.nanosecondsToSeconds(System.nanoTime());

      double overslept = (after - before) - sleepDuration;

      // FIXME?
//      LogTools.info("Overslept %f ms".formatted(Conversions.secondsToMilliseconds(overslept)));

      assertTrue(overslept < 0.005); // Assert we don't oversleep more than 5 milliseconds -- typically a lot lower

      return overslept > 0.0;
   }

   @Test
   public void testCancellableScheduledTasks()
   {
      ScheduledExecutorService scheduler = ThreadTools.newSingleDaemonThreadScheduledExecutor("Test");

      StringBuilder output = new StringBuilder();

      ScheduledFuture<?> scheduledFuture1 = scheduler.schedule(() -> output.append("A"), 400, TimeUnit.MILLISECONDS);
      ThreadTools.sleep(200);
      scheduledFuture1.cancel(false);
      scheduler.schedule(() -> output.append("B"), 400, TimeUnit.MILLISECONDS);
      ThreadTools.sleep(600);
      ScheduledFuture<StringBuilder> scheduledFuture2 = scheduler.schedule(() -> output.append("C"), 400, TimeUnit.MILLISECONDS);
      ThreadTools.sleep(200);
      scheduledFuture2.cancel(false);
      ThreadTools.sleep(600);
      scheduler.schedule(() -> output.append("D"), 400, TimeUnit.MILLISECONDS);
      ThreadTools.sleep(600);

      scheduler.schedule(() -> ExceptionTools.handle(() ->
      {
         output.append("E");
         throw new NullPointerException();
      }, DefaultExceptionHandler.PRINT_MESSAGE), 400, TimeUnit.MILLISECONDS);
      ThreadTools.sleep(600);
      ScheduledFuture<StringBuilder> scheduledFuture3 = scheduler.schedule(() -> output.append("F"), 400, TimeUnit.MILLISECONDS);
      ThreadTools.sleep(200);
      scheduledFuture3.cancel(false);
      ThreadTools.sleep(600);

      String recordedOutput = output.toString();
      assertEquals("BDE", recordedOutput);
      LogTools.info(recordedOutput);

      scheduler.shutdown();
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
