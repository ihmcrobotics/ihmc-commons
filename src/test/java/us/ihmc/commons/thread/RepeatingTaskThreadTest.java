package us.ihmc.commons.thread;

import org.junit.jupiter.api.Test;
import us.ihmc.commons.time.FrequencyCalculator;
import us.ihmc.log.LogTools;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RepeatingTaskThreadTest
{
   private static final String NAME = "TestRepeatingTaskThread";

   @Test
   public void testStartKill()
   {
      AtomicBoolean taskRan = new AtomicBoolean(false);
      RepeatingTaskThread thread = new RepeatingTaskThread(() -> taskRan.set(true), NAME);

      // Start the thread, but don't run anything
      thread.start();
      assertState(thread, true, true, 0, false, 0);

      // Kill the thread, wait for it to die
      thread.kill();
      assertState(thread, false, true, 0, false, 0);
      assertDoesNotThrow(() -> thread.join(1000));
      assertState(thread, false, false, 0, false, 0);

      // Ensure the task never ran
      assertFalse(taskRan.get());
   }

   @Test
   public void testStartRepeatKill()
   {
      AtomicInteger repetitions = new AtomicInteger(0);
      RepeatingTaskThread thread = new RepeatingTaskThread(() ->
      {
         LogTools.info("Repetition {}", repetitions.getAndIncrement());
         Thread.sleep(10);
      }, NAME);

      // Set the thread to run 10 repetitions
      int repetitionsToRun = 10;
      thread.setScheduled(repetitionsToRun);
      assertState(thread, false, false, repetitionsToRun, false, 0);

      // Start the thread. Should start running the repetitions
      thread.start();
      assertState(thread, true, true, repetitionsToRun, false, 0);

      // Wait for all repetitions to complete
      thread.blockUntilNoScheduledTasks();
      assertState(thread, true, true, 0, false, repetitionsToRun);

      // Kill the thread and wait for it to die
      thread.kill();
      assertState(thread, false, true, 0, false, repetitionsToRun);
      assertDoesNotThrow(() -> thread.join(1000));
      assertState(thread, false, false, 0, false, repetitionsToRun);

      assertEquals(repetitionsToRun, repetitions.get());
   }

   @Test
   public void testStartPauseStart()
   {
      AtomicInteger repetitions = new AtomicInteger(0);
      RepeatingTaskThread thread = new RepeatingTaskThread(() ->
      {
         LogTools.info("Repetition {}", repetitions.getAndIncrement());
         ThreadTools.park(0.01);
      }, NAME);

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
      assertState(thread, false, true, RepeatingTaskThread.REPEAT_INDEFINITELY, true, 1);
      assertDoesNotThrow(() -> thread.join(1000));
      assertState(thread, false, false, RepeatingTaskThread.REPEAT_INDEFINITELY, false, 2);
   }

   @Test
   public void testDoubleStart()
   {
      AtomicInteger repetitions = new AtomicInteger(0);
      RepeatingTaskThread thread = new RepeatingTaskThread(() ->
      {
         LogTools.info("Repetition {}", repetitions.getAndIncrement());
         ThreadTools.park(0.01);
      }, NAME);

      thread.startRepeating();
      assertState(thread, true, true, RepeatingTaskThread.REPEAT_INDEFINITELY, false, 0);

      thread.startRepeating();
      assertState(thread, true, true, RepeatingTaskThread.REPEAT_INDEFINITELY, false, 0);

      ThreadTools.park(0.005);

      thread.kill();
      assertDoesNotThrow(() -> thread.join(1000));
      assertState(thread, false, false, RepeatingTaskThread.REPEAT_INDEFINITELY, false, 1);
   }

   @Test
   public void testKillWithoutStart()
   {
      AtomicInteger repetitions = new AtomicInteger(0);
      RepeatingTaskThread thread = new RepeatingTaskThread(() ->
      {
         LogTools.info("Repetition {}", repetitions.getAndIncrement());
         Thread.sleep(10);
      }, NAME);

      assertState(thread, false, false, 0, false, 0);

      thread.blockingKill();
      assertState(thread, false, false, 0, false, 0);

      thread.blockingKill();
      assertState(thread, false, false, 0, false, 0);
   }

   @Test
   public void testAddScheduledRepetitions()
   {
      AtomicInteger repetitions = new AtomicInteger(0);
      RepeatingTaskThread thread = new RepeatingTaskThread(repetitions::getAndIncrement, NAME);

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
      RepeatingTaskThread thread = new RepeatingTaskThread(frequencyCalculator::ping, NAME).setFrequencyLimit(targetFrequency);

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
         protected void runTask() throws Throwable
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
