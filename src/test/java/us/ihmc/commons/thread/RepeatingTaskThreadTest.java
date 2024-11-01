package us.ihmc.commons.thread;

import org.junit.jupiter.api.Test;
import us.ihmc.commons.time.FrequencyCalculator;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

// TODO: fix/adjust
public class RepeatingTaskThreadTest
{
   private static final String NAME = "TestLoopingThread";

   @Test
   public void testStartKill()
   {
      RepeatingTaskThread thread = new RepeatingTaskThread(() ->
      {
         System.out.println("Test Thread Running");
         Thread.sleep(500);
      }, NAME);

      thread.start();
      assertTrue(thread.isLooping());
      assertTrue(thread.isAlive());

      thread.kill();
      try
      {
         Thread.sleep(1000);
      }
      catch (InterruptedException e)
      {
         throw new RuntimeException(e);
      }
      assertFalse(thread.isLooping());
      assertFalse(thread.isAlive());
   }

   @Test
   public void testStartPauseStart()
   {
      RepeatingTaskThread thread = new RepeatingTaskThread(() ->
      {
         System.out.println("Test Thread Running");
         Thread.sleep(500);
      }, NAME);

      thread.start();
      assertTrue(thread.isLooping());
      assertTrue(thread.isAlive());

      thread.stopRepeating();
      try
      {
         Thread.sleep(1000);
      }
      catch (InterruptedException e)
      {
         throw new RuntimeException(e);
      }
      assertFalse(thread.isLooping());
      assertTrue(thread.isAlive());

      thread.startRepeating();
      assertTrue(thread.isLooping());
      assertTrue(thread.isAlive());

      thread.kill();
      try
      {
         Thread.sleep(1000);
      }
      catch (InterruptedException e)
      {
         throw new RuntimeException(e);
      }
      assertFalse(thread.isLooping());
      assertFalse(thread.isAlive());
   }

   @Test
   public void testDoubleStart()
   {
      RepeatingTaskThread thread = new RepeatingTaskThread(() ->
      {
         System.out.println("Test Thread Running");
         Thread.sleep(500);
      }, NAME);

      thread.start();
      assertTrue(thread.isLooping());
      assertTrue(thread.isAlive());

      thread.start();
      assertTrue(thread.isLooping());
      assertTrue(thread.isAlive());

      thread.kill();
      try
      {
         Thread.sleep(1000);
      }
      catch (InterruptedException e)
      {
         throw new RuntimeException(e);
      }
      assertFalse(thread.isLooping());
      assertFalse(thread.isAlive());
   }

   @Test
   public void testDoubleDestroy()
   {
      RepeatingTaskThread thread = new RepeatingTaskThread(() ->
      {
         System.out.println("Test Thread Running");
         Thread.sleep(500);
      }, NAME);

      thread.start();
      assertTrue(thread.isLooping());
      assertTrue(thread.isAlive());

      thread.kill();
      try
      {
         Thread.sleep(1000);
      }
      catch (InterruptedException e)
      {
         throw new RuntimeException(e);
      }
      assertFalse(thread.isLooping());
      assertFalse(thread.isAlive());

      thread.kill();
      try
      {
         Thread.sleep(1000);
      }
      catch (InterruptedException e)
      {
         throw new RuntimeException(e);
      }
      assertFalse(thread.isLooping());
      assertFalse(thread.isAlive());
   }

   @Test
   public void testDestroyWithoutStart()
   {
      RepeatingTaskThread thread = new RepeatingTaskThread(() ->
      {
         System.out.println("Test Thread Running");
         Thread.sleep(500);
      }, NAME);

      thread.blockingKill();
      assertFalse(thread.isLooping());
      assertFalse(thread.isAlive());
   }

   @Test
   public void testBlockingDestroy()
   {
      RepeatingTaskThread thread = new RepeatingTaskThread(() ->
      {
         System.out.println("Test Thread Running");
         Thread.sleep(500);
      }, NAME);

      thread.start();
      assertTrue(thread.isLooping());
      assertTrue(thread.isAlive());

      thread.blockingKill();
      assertFalse(thread.isLooping());
      assertFalse(thread.isAlive());
   }

   @Test
   public void testLoopOnce()
   {
      AtomicInteger loopCounter = new AtomicInteger(0);
      RepeatingTaskThread thread = new RepeatingTaskThread(() ->
      {
         assert loopCounter.incrementAndGet() == 1;
      }, NAME);

      thread.setRemaining(1);
      ThreadTools.sleep(500);
      thread.blockingKill();
      assertEquals(1, loopCounter.get());
      assertFalse(thread.isLooping());
      assertFalse(thread.isAlive());
   }

   @Test
   public void testLoopNIterations()
   {
      AtomicInteger loopCounter = new AtomicInteger(0);
      Notification loopedNotification = new Notification();
      Notification loopAssertedNotification = new Notification();

      RepeatingTaskThread thread = new RepeatingTaskThread(() ->
      {
         loopCounter.set(loopCounter.get() + 1);
         loopedNotification.set();
         loopAssertedNotification.blockingPoll();
      }, NAME);

      for (int targetLoops = 1; targetLoops < 25; ++targetLoops)
      {
         loopCounter.set(0);
         thread.setRemaining(targetLoops);
         for (int i = 0; i < targetLoops; ++i)
         {
            loopedNotification.blockingPoll();
            assertEquals(i + 1, loopCounter.get());
            loopAssertedNotification.set();
         }
      }

      thread.blockingKill();
   }

   @Test
   public void testAddIterations()
   {
      AtomicInteger loopCounter = new AtomicInteger(0);
      RepeatingTaskThread thread = new RepeatingTaskThread(loopCounter::getAndIncrement, NAME);

      int add = 20;
      int subtract = -10;
      int increment = 1;
      int total = add + subtract + increment;

      thread.addRemaining(add);
      thread.addRemaining(subtract);
      thread.addRemaining(increment);
      ThreadTools.sleep(500);
      thread.blockingKill();
      assertEquals(total, loopCounter.get());
   }

   @Test
   public void testLoopFrequencyLimit()
   {
      FrequencyCalculator frequencyCalculator = new FrequencyCalculator();

      double targetFrequency = 5.0;
      RepeatingTaskThread thread = new RepeatingTaskThread(frequencyCalculator::ping, targetFrequency, NAME);

      thread.start();
      ThreadTools.sleep(5000);
      assertEquals(targetFrequency, frequencyCalculator.getFrequency(), 0.1);

      targetFrequency = 30.0;
      thread.setFrequencyLimit(targetFrequency);
      ThreadTools.sleep(5000);
      assertEquals(targetFrequency, frequencyCalculator.getFrequency(), 0.1);

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
            synchronized (interruptCount)
            {
               interruptCount.incrementAndGet();
               interruptCount.notify();
            }
         }
      }, NAME);

      // Test during free spin
      thread.start();
      for (int i = 0; i < 100; ++i)
      {
         thread.interrupt();
         synchronized (interruptCount)
         {
            interruptCount.wait(500);
         }
         assertEquals(i + 1, interruptCount.get());
      }

      // Test during throttled looping
      interruptCount.set(0);
      thread.setFrequencyLimit(5.0);
      for (int i = 0; i < 50; ++i)
      {
         thread.interrupt();
         synchronized (interruptCount)
         {
            interruptCount.wait(500);
         }
         assertEquals(i + 1, interruptCount.get());
      }

      // Test during pause
      interruptCount.set(0);
      thread.stopRepeating();
      for (int i = 0; i < 100; ++i)
      {
         thread.interrupt();
         synchronized (interruptCount)
         {
            interruptCount.wait(500);
         }
         assertEquals(i + 1, interruptCount.get());
      }
      thread.blockingKill();
   }

   @Test
   public void testOverride()
   {
      AtomicInteger loopCounter = new AtomicInteger(0);
      RepeatingTaskThread thread = new RepeatingTaskThread(NAME)
      {
         @Override
         protected void repeat()
         {
            loopCounter.set(loopCounter.get() + 1);
         }
      };

      int targetLoops = 15;
      thread.setRemaining(targetLoops);
      ThreadTools.sleep(500);
      thread.blockingKill();
      assertEquals(targetLoops, loopCounter.get());
   }
}
