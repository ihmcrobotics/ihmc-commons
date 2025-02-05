package us.ihmc.commons.thread;

import org.junit.jupiter.api.Test;
import us.ihmc.commons.Conversions;
import us.ihmc.commons.RunnableThatThrows;
import us.ihmc.commons.time.FrequencyCalculator;
import us.ihmc.log.LogTools;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.*;

public class RepeatingTaskThreadTest
{
   private static final String NAME = "TestRepeatingTaskThread";

   @Test
   public void testStartKill()
   {
      AtomicBoolean taskRan = new AtomicBoolean(false);
      RepeatingTaskThread thread = new RepeatingTaskThread(NAME, () -> taskRan.set(true));

      // Start the thread, but don't run anything
      thread.start();
      assertState(thread, true, true, 0, false, 0);

      // Kill the thread, wait for it to die
      thread.kill();
      assertDoesNotThrow(() -> thread.join(1000));
      assertState(thread, false, false, 0, false, 0);

      // Ensure the task never ran
      assertFalse(taskRan.get());
   }

   @Test
   public void testStartRepeatKill()
   {
      AtomicInteger repetitions = new AtomicInteger(0);
      RepeatingTaskThread thread = new RepeatingTaskThread(NAME, () ->
      {
         LogTools.info("Repetition {}", repetitions.getAndIncrement());
         Thread.sleep(10);
      });

      // Set the thread to run 10 repetitions
      int repetitionsToRun = 10;
      thread.setScheduled(repetitionsToRun);
      assertState(thread, false, false, repetitionsToRun, false, 0);

      // Start the thread. Should start running the repetitions
      thread.start();

      // Wait for all repetitions to complete
      thread.blockUntilNoScheduledTasks();
      assertState(thread, true, true, 0, false, repetitionsToRun);

      // Kill the thread and wait for it to die
      thread.kill();
      assertDoesNotThrow(() -> thread.join(1000));
      assertState(thread, false, false, 0, false, repetitionsToRun);

      assertEquals(repetitionsToRun, repetitions.get());
   }

   @Test
   public void testStartPauseStart()
   {
      AtomicInteger repetitions = new AtomicInteger(0);
      RepeatingTaskThread thread = new RepeatingTaskThread(NAME, () ->
      {
         LogTools.info("Repetition {}", repetitions.getAndIncrement());
         ThreadTools.park(0.01);
      });

      // Start repeating
      thread.startRepeating();
      assertState(thread, true, true, RepeatingTaskThread.REPEAT_INDEFINITELY, false, 0);

      // Ensure a task starts
      thread.blockUntilNextTaskExecution();
      assertState(thread, true, true, RepeatingTaskThread.REPEAT_INDEFINITELY, true, 0);

      // Stop repeating
      thread.stopRepeating();
      assertState(thread, true, true, 0, true, 0);

      thread.blockUntilNextTaskCompletion();
      assertState(thread, true, true, 0, false, 1);

      // Start again
      thread.startRepeating();
      assertState(thread, true, true, RepeatingTaskThread.REPEAT_INDEFINITELY, false, 1);

      ThreadTools.park(0.005);

      // Kill the thread
      thread.kill();
      assertDoesNotThrow(() -> thread.join(1000));
      assertState(thread, false, false, 0, false, 2);
   }

   @Test
   public void testDoubleStart()
   {
      AtomicInteger repetitions = new AtomicInteger(0);
      RepeatingTaskThread thread = new RepeatingTaskThread(NAME, () ->
      {
         LogTools.info("Repetition {}", repetitions.getAndIncrement());
         ThreadTools.park(0.01);
      });

      thread.startRepeating();
      assertState(thread, true, true, RepeatingTaskThread.REPEAT_INDEFINITELY, false, 0);

      thread.startRepeating();
      assertState(thread, true, true, RepeatingTaskThread.REPEAT_INDEFINITELY, false, 0);

      ThreadTools.park(0.005);

      thread.kill();
      assertDoesNotThrow(() -> thread.join(1000));
      assertState(thread, false, false, 0, false, 1);
   }

   @Test
   public void testKillWithoutStart()
   {
      AtomicInteger repetitions = new AtomicInteger(0);
      RepeatingTaskThread thread = new RepeatingTaskThread(NAME, () ->
      {
         LogTools.info("Repetition {}", repetitions.getAndIncrement());
         Thread.sleep(10);
      });

      assertState(thread, false, false, 0, false, 0);

      thread.blockingKill();
      assertState(thread, false, false, 0, false, 0);

      thread.blockingKill();
      assertState(thread, false, false, 0, false, 0);
   }

   @Test
   public void testInvalidStarts()
   {
      AtomicInteger repetitions = new AtomicInteger(0);
      RunnableThatThrows runnable = () ->
      {
         LogTools.info("Repetition {}", repetitions.getAndIncrement());
         Thread.sleep(10);
      };

      // Cannot call start when the thread is already running
      RepeatingTaskThread thread1 = new RepeatingTaskThread(NAME, runnable);
      thread1.startRepeating();
      assertThrows(IllegalThreadStateException.class, thread1::start);
      thread1.kill();

      // Cannot restart after killing
      RepeatingTaskThread thread2 = new RepeatingTaskThread(NAME, runnable);
      thread2.startRepeating();
      thread2.blockingKill();
      assertThrows(IllegalThreadStateException.class, thread2::startRepeating);
   }

   @Test
   public void testAddScheduledRepetitions()
   {
      AtomicInteger repetitions = new AtomicInteger(0);
      RepeatingTaskThread thread = new RepeatingTaskThread(NAME, repetitions::getAndIncrement);

      int add = 20;
      int subtract = -10;
      int increment = 1;
      int total = add + subtract + increment;

      thread.addScheduled(add);
      thread.addScheduled(subtract);
      thread.start();

      thread.addScheduled(increment);
      thread.blockUntilNoScheduledTasks();
      thread.kill();
      assertEquals(total, repetitions.get());
      assertEquals(total, thread.getCompleted());
   }

   @Test
   public void testLoopFrequencyLimit()
   {
      FrequencyCalculator frequencyCalculator = new FrequencyCalculator();

      double targetFrequency = 5.0;
      RepeatingTaskThread thread = new RepeatingTaskThread(NAME, frequencyCalculator::ping).setFrequencyLimit(targetFrequency);

      // Start repeating at the target frequency
      thread.startRepeating();
      ThreadTools.sleep(750);
      assertEquals(targetFrequency, frequencyCalculator.getFrequency(), 0.2);

      // Increase the target frequency
      targetFrequency = 30.0;
      thread.setFrequencyLimit(targetFrequency);
      ThreadTools.sleep(750);
      assertEquals(targetFrequency, frequencyCalculator.getFrequency(), 0.2);

      // Un-limit the repetition frequency
      thread.removeFrequencyLimit();
      ThreadTools.sleep(750);
      assertTrue(frequencyCalculator.getFrequency() > targetFrequency + 10.0); // Ensure thread is running at higher frequency than previous limit

      thread.blockingKill();
   }

   @Test
   public void testInterrupt()
   {
      AtomicInteger interruptCount = new AtomicInteger(0);
      RepeatingTaskThread thread = new RepeatingTaskThread(NAME)
      {
         @Override
         protected void runTask()
         {
            ThreadTools.park(0.01);

            if (interrupted())
               interruptCount.incrementAndGet();
         }
      };

      LogTools.info("Test during free spin");
      thread.startRepeating();
      for (int i = 0; i < 25; ++i)
      {
         thread.blockUntilNextTaskExecution();
         thread.interrupt();
         thread.blockUntilNextTaskCompletion();
         assertEquals(i + 1, interruptCount.get());
      }

      LogTools.info("Test during throttled looping");
      interruptCount.set(0);
      thread.setFrequencyLimit(200.0);
      for (int i = 0; i < 25; ++i)
      {
         thread.blockUntilNextTaskExecution();
         thread.interrupt();
         thread.blockUntilNextTaskCompletion();
         assertEquals(i + 1, interruptCount.get());
      }

      thread.stopRepeating();

      LogTools.info("Test one by one");
      interruptCount.set(0);
      for (int i = 0; i < 25; ++i)
      {
         thread.addScheduled(1);
         thread.blockUntilNextTaskExecution();
         thread.interrupt();
         thread.blockUntilNextTaskCompletion();
         assertEquals(i + 1, interruptCount.get());
      }

      LogTools.info("Completed test");

      thread.blockingKill();
   }

   @Test
   public void testImmediateShutdown()
   {
      RunnableThatThrows wasteTime = () ->
      {
         try
         {  // Sleep for half a second
            Thread.sleep((long) Conversions.secondsToMilliseconds(0.5));
         } catch (InterruptedException ignored) {}
      };

      LogTools.info("Test during free spin");
      for (int millisToSleep = 0; millisToSleep < 500; millisToSleep += 100)
      {
         // Create a new thread
         RepeatingTaskThread thread = new RepeatingTaskThread(NAME, wasteTime);

         // Start free spin
         thread.startRepeating();
         ThreadTools.sleep(millisToSleep);

         // Time the shutdown duration
         long shutdownStart = System.nanoTime();
         thread.kill();
         thread.interrupt();
         assertDoesNotThrow(() -> thread.join(500));
         long shutdownComplete = System.nanoTime();

         double shutdownDuration = Conversions.nanosecondsToSeconds(shutdownComplete - shutdownStart);
         LogTools.info("Shutdown Duration: {}", shutdownDuration);
         assertTrue(shutdownDuration < 0.01);
      }

      LogTools.info("Test during throttled looping");
      for (int millisToSleep = 0; millisToSleep < 500; millisToSleep += 100)
      {
         // Create a new throttled thread
         RepeatingTaskThread thread = new RepeatingTaskThread(NAME, wasteTime).setFrequencyLimit(1.0);

         // Start throttled spin
         thread.startRepeating();
         ThreadTools.sleep(millisToSleep);

         // Time the shutdown duration
         long shutdownStart = System.nanoTime();
         thread.kill();
         thread.interrupt();
         assertDoesNotThrow(() -> thread.join(500));
         long shutdownComplete = System.nanoTime();

         double shutdownDuration = Conversions.nanosecondsToSeconds(shutdownComplete - shutdownStart);
         LogTools.info("Shutdown Duration: {}", shutdownDuration);
         assertTrue(shutdownDuration < 0.01);
      }

      LogTools.info("Test during pause");
      for (int millisToSleep = 0; millisToSleep < 500; millisToSleep += 100)
      {
         // Create a new throttled thread
         RepeatingTaskThread thread = new RepeatingTaskThread(NAME, wasteTime);

         // Start throttled spin
         thread.start();
         ThreadTools.sleep(millisToSleep);

         // Time the shutdown duration
         long shutdownStart = System.nanoTime();
         thread.kill();
         // No interrupted necessary in this case
         assertDoesNotThrow(() -> thread.join(500));
         long shutdownComplete = System.nanoTime();

         double shutdownDuration = Conversions.nanosecondsToSeconds(shutdownComplete - shutdownStart);
         LogTools.info("Shutdown Duration: {}", shutdownDuration);
         assertTrue(shutdownDuration < 0.01);
      }
   }

   @Test
   public void testImmediateShutdownRace()
   {
      RunnableThatThrows wasteTime = () ->
      {
         try
         {  // Sleep for 5 seconds
            Thread.sleep(5000);
         } catch (InterruptedException ignored) {}
      };

      for (int i = 0; i < 5000; ++i)
      {
         // Create a new thread
         RepeatingTaskThread thread = new RepeatingTaskThread(NAME, wasteTime);

         // Start free spin
         thread.startRepeating();
         LockSupport.parkNanos(5);

         // Time the shutdown duration
         long shutdownStart = System.nanoTime();
         thread.kill();
         thread.interrupt();
         assertDoesNotThrow(() -> thread.join(500));
         long shutdownComplete = System.nanoTime();

         double shutdownDuration = Conversions.nanosecondsToSeconds(shutdownComplete - shutdownStart);
         LogTools.info("Shutdown Duration: {}", shutdownDuration);
         assertTrue(shutdownDuration < 0.01);
      }
   }

   @Test
   public void testOverride()
   {
      AtomicInteger loopCounter = new AtomicInteger(0);
      RepeatingTaskThread thread = new RepeatingTaskThread(NAME)
      {
         @Override
         protected void runTask()
         {
            loopCounter.set(loopCounter.get() + 1);
         }
      };

      int targetLoops = 15;
      thread.setScheduled(targetLoops);
      thread.start();
      thread.blockUntilNoScheduledTasks();
      thread.blockingKill();
      assertEquals(targetLoops, loopCounter.get());
   }

   private void assertState(RepeatingTaskThread thread, boolean running, boolean isAlive, long scheduled, boolean isExecuting, long completed)
   {
      assertEquals(running, thread.isRunning());
      assertEquals(isAlive, thread.isAlive());
      assertEquals(scheduled, thread.getScheduled());
      assertEquals(isExecuting, thread.isExecuting());
      assertEquals(completed, thread.getCompleted());
   }
}
