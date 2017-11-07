package us.ihmc.commons.thread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import us.ihmc.commons.thread.ThreadTools;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.IntegrationCategory;

public class ThreadToolsTest
{
   @ContinuousIntegrationTest(estimatedDuration = 3.1, categoriesOverride = IntegrationCategory.FLAKY)
   @Test(timeout = 30000)
   public void testTimeLimitScheduler()
   {
      final int ITERATIONS = 100;
      final double EPSILON = 5;

      TimeUnit timeUnit = TimeUnit.MILLISECONDS;
      long initialDelay = 0;
      long delay = 3;
      long timeLimit = 30;

      final Runnable runnable = new Runnable()

      {
         @Override
         public void run()
         {
            //Do some calculation
            Math.sqrt(Math.PI);
         }
      };

      for (int i = 0; i < ITERATIONS; i++)
      {
         long startTime = System.currentTimeMillis();
         ScheduledFuture<?> future = ThreadTools.scheduleWithFixeDelayAndTimeLimit(getClass().getSimpleName(), runnable, initialDelay, delay, timeUnit,
                                                                                   timeLimit);
         while (!future.isDone())
         {
            // do nothing
         }
         long endTime = System.currentTimeMillis();
         assertEquals(timeLimit, endTime - startTime, EPSILON);
      }
   }

   @ContinuousIntegrationTest(estimatedDuration = 0.1)
   @Test(timeout = 30000)
   public void testIterationLimitScheduler()
   {
      TimeUnit timeUnit = TimeUnit.MILLISECONDS;
      long initialDelay = 0;
      long delay = 10;
      final int iterations = 10;

      final AtomicInteger counter = new AtomicInteger();

      final Runnable runnable = new Runnable()
      {
         @Override
         public void run()
         {
            counter.incrementAndGet();
         }
      };

      ScheduledFuture<?> future = ThreadTools.scheduleWithFixedDelayAndIterationLimit(getClass().getSimpleName(), runnable, initialDelay, delay, timeUnit,
                                                                                      iterations);

      while (!future.isDone())
      {
         // do nothing
      }

      assertEquals(iterations, counter.get());
   }

   @ContinuousIntegrationTest(estimatedDuration = 0.1, categoriesOverride = IntegrationCategory.FLAKY)
   @Test(timeout = 30000)
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
         assertFalse("Didn't run. Should timeout.", holder.state.equals(State.DIDNT_RUN));
         assertTrue("Did not timeout.", holder.state.equals(State.TIMED_OUT));

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
         assertFalse("Didn't run. Shouldn't timeout.", holder.state.equals(State.DIDNT_RUN));
         assertTrue("Timed out early.", holder.state.equals(State.RAN_WITHOUT_TIMING_OUT));
      }
   }

   @ContinuousIntegrationTest(estimatedDuration = 3.0)
   @Test(timeout = 30000)
   public void testThreadSleepEvenWhenInterrupted()
   {
      long oneMillion = 1000000;
      long millisecondsToSleep = 1100;
      int additionalNanosecondsToSleep = 500000;

      long totalNanosecondsToSleep = millisecondsToSleep * oneMillion + additionalNanosecondsToSleep;

      SleepAndVerifyDespiteWakingUpRunnable runnable = new SleepAndVerifyDespiteWakingUpRunnable(millisecondsToSleep, additionalNanosecondsToSleep);

      Thread thread = new Thread(runnable);
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

         // Here we interrupt the thread to make sure that it sleeps for the total amount of time and so that it also re-interrupts the 
         // thread after it is done sleeping.
         thread.interrupt();
      }

      // Yield this thread to make sure the other one has an opportunity to wake up during its second sleep.
      Thread.yield();
      assertTrue(runnable.wasInterruptedDuringSecondSleep());

      long timeSleptInNanoseconds = runnable.getTimeSleptNanonseconds();
      long timeOverSleptInNanoseconds = timeSleptInNanoseconds - totalNanosecondsToSleep;

      // Check to make sure slept at least the amount specified.
      assertTrue("timeSlept = " + timeSleptInNanoseconds + ", totalNanosecondsToSleep = " + totalNanosecondsToSleep, timeOverSleptInNanoseconds >= 0);
      
      // Check to make sure didn't over sleep by more than 5 milliseconds, which seems reasonable on most operating systems.
      assertTrue("timeSlept = " + timeSleptInNanoseconds + ", millisecondsToSleep = " + millisecondsToSleep, timeOverSleptInNanoseconds < 100 * oneMillion);

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

      // Yield this thread to make sure the other one has an opportunity to wake up during its second sleep, if it is interrupted (which it shouldn't be).
      Thread.yield();
      assertFalse(runnable.wasInterruptedDuringSecondSleep());
   }

   private class SleepAndVerifyDespiteWakingUpRunnable implements Runnable
   {
      private long millisecondsToSleep;
      private int additonalNanosecondsToSleep;
      private boolean isDoneSleeping;
      private boolean wasInterruptedDuringSecondSleep;
      private long timeSleptNanoseconds;

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
         return timeSleptNanoseconds;
      }

      public boolean wasInterruptedDuringSecondSleep()
      {
         return wasInterruptedDuringSecondSleep;
      }

      @Override
      public void run()
      {
         long startTime = System.nanoTime();

         ThreadTools.sleep(millisecondsToSleep, additonalNanosecondsToSleep);

         long endTime = System.nanoTime();
         timeSleptNanoseconds = endTime - startTime;
         isDoneSleeping = true;

         try
         {
            Thread.sleep(100000);
         }
         catch (InterruptedException e)
         {
            wasInterruptedDuringSecondSleep = true;
         }
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
