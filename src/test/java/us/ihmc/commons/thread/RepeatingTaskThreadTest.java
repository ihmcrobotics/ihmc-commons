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
   public void testStartKill() throws InterruptedException
   {
      AtomicBoolean taskRan = new AtomicBoolean(false);
      RepeatingTaskThread thread = new RepeatingTaskThread(() -> taskRan.set(true), NAME);

      // Start the thread, but don't run anything
      thread.start();
      assertCorrectState(thread, true, false);

      // Kill the thread, wait for it to die
      thread.kill();
      thread.join(1000);
      assertCorrectState(thread, false, false);

      // Ensure the task never ran
      assertFalse(taskRan.get());
      assertEquals(0L, thread.getCompleted());
   }

   @Test
   public void testStartRepeatKill() throws InterruptedException
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
      assertCorrectState(thread, false, false);
      assertEquals(repetitionsToRun, thread.getScheduled());

      // Start the thread. Should start running the repetitions
      thread.start();
      assertCorrectState(thread, true, true);

      // Wait for all repetitions to complete
      thread.blockUntilNoScheduledTasks();
      assertEquals(0, thread.getScheduled());
      assertCorrectState(thread, true, false);

      // Kill the thread and wait for it to die
      thread.kill();
      thread.join(1000);
      assertCorrectState(thread, false, false);

      assertEquals(repetitionsToRun, thread.getCompleted());
      assertEquals(repetitionsToRun, repetitions.get());
   }

   @Test
   public void testStartPauseStart() throws InterruptedException
   {
      AtomicInteger repetitions = new AtomicInteger(0);
      RepeatingTaskThread thread = new RepeatingTaskThread(() ->
      {
         LogTools.info("Repetition {}", repetitions.getAndIncrement());
         Thread.sleep(10);
      }, NAME);

      // Start repeating
      thread.startRepeating();
      assertCorrectState(thread, true, true);
      assertEquals(RepeatingTaskThread.REPEAT_INDEFINITELY, thread.getScheduled());

      // Ensure a task starts
      thread.blockUntilNextTaskExecution();

      // Stop repeating
      thread.stopRepeating();
      assertCorrectState(thread, true, false);

      thread.blockUntilNextTaskCompletion();
      assertTrue(thread.getCompleted() > 0);
      assertEquals(0, thread.getScheduled());

      // Start again
      thread.startRepeating();
      assertCorrectState(thread, true, true);
      assertEquals(RepeatingTaskThread.REPEAT_INDEFINITELY, thread.getScheduled());

      // Kill the thread
      thread.kill();
      thread.join(1000);
      assertCorrectState(thread, false, false);
   }

   @Test
   public void testDoubleStart() throws InterruptedException
   {
      AtomicInteger repetitions = new AtomicInteger(0);
      RepeatingTaskThread thread = new RepeatingTaskThread(() ->
      {
         LogTools.info("Repetition {}", repetitions.getAndIncrement());
         Thread.sleep(10);
      }, NAME);

      thread.startRepeating();
      assertCorrectState(thread, true, true);

      thread.startRepeating();
      assertCorrectState(thread, true, true);

      thread.kill();
      thread.join(1000);
      assertCorrectState(thread, false, false);
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

      assertCorrectState(thread, false, false);

      thread.blockingKill();
      assertCorrectState(thread, false, false);

      thread.blockingKill();
      assertCorrectState(thread, false, false);
   }

   @Test
   public void testAddScheduledRepetitions() throws InterruptedException
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
   public void testInterrupt() throws InterruptedException
   {
      AtomicInteger interruptCount = new AtomicInteger(0);
      RepeatingTaskThread thread = new RepeatingTaskThread(() ->
      {
         try
         {
            Thread.sleep(10);
         }
         catch (InterruptedException interruptedException)
         {
            interruptCount.incrementAndGet();
         }
      }, NAME);

      // Test during free spin
      thread.startRepeating();
      for (int i = 0; i < 25; ++i)
      {
         thread.interrupt();
         thread.blockUntilNextTaskCompletion();
         assertEquals(i + 1, interruptCount.get());
      }

      // Test during throttled looping
      interruptCount.set(0);
      thread.setFrequencyLimit(5.0);
      for (int i = 0; i < 25; ++i)
      {
         thread.interrupt();
         thread.blockUntilNextTaskCompletion();
         assertEquals(i + 1, interruptCount.get());
      }

      // Test during pause
      interruptCount.set(0);
      thread.stopRepeating();
      for (int i = 0; i < 25; ++i)
      {
         thread.interrupt();
         thread.blockUntilNextTaskCompletion();
         assertEquals(i + 1, interruptCount.get());
      }
      thread.blockingKill();
   }

   @Test
   public void testOverride() throws InterruptedException
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

   private void assertCorrectState(RepeatingTaskThread thread, boolean shouldBeAlive, boolean shouldBeRepeating)
   {
      assertEquals(shouldBeAlive, thread.isAlive());
      assertEquals(shouldBeRepeating, thread.isRepeating());
   }
}
