package us.ihmc.commons.thread;

import org.junit.jupiter.api.Test;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.time.Stopwatch;
import us.ihmc.log.LogTools;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class TypedNotificationTest
{
   @Test
   public void testNotified()
   {
      TypedNotification<Integer> notification = new TypedNotification<>();

      assertTrue(notification.peek() == null);
      assertFalse(notification.peekHasValue());
      assertFalse(notification.poll());
      assertFalse(notification.hasValue());
      assertNull(notification.read());

      notification.set(6);

      assertTrue(notification.peekHasValue());
      assertEquals(6, notification.peek());
      assertTrue(notification.poll());
      assertTrue(notification.hasValue());
      assertEquals(6, notification.read());
   }

   @Test
   public void testNotifiedBeforeBlockingPollCalled()
   {
      TypedNotification<Object> notification = new TypedNotification<>();

      notification.set(new Object());
      assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
      {
         notification.blockingPoll();
      });

      assertNotNull(notification.read());
   }

   @Test
   public void testNotificationFromThread()
   {
      assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
      {
         TypedNotification<Integer> notification = new TypedNotification<>();

         assertFalse(notification.poll());
         assertNull(notification.read());

         double secondsToSleep = 0.2;

         Stopwatch stopwatch = new Stopwatch().start();
         ThreadTools.startAThread(() ->
         {
            ThreadTools.sleepSeconds(secondsToSleep);
            notification.set(8);
         }, "SetterThread");

         assertEquals(8, notification.blockingPoll());

         double elapsed = stopwatch.totalElapsed();

         LogTools.info("Elapsed: {}", elapsed);
         assertEquals(secondsToSleep, elapsed, 0.1);

         assertEquals(8, notification.read());
      });
   }

   @Test
   public void testInterruptedWhileBlocking()
   {
      assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
      {
         TypedNotification<Integer> notification = new TypedNotification();

         assertFalse(notification.poll());
         assertEquals(null, notification.read());

         Stopwatch stopwatch = new Stopwatch().start();
         Thread blockingThread = ThreadTools.startAThread(() ->
         {
            RuntimeException runtimeException = assertThrows(RuntimeException.class, () ->
            {
               notification.blockingPoll(DefaultExceptionHandler.RUNTIME_EXCEPTION);
            });
            assertEquals(InterruptedException.class, runtimeException.getCause().getClass());

            stopwatch.suspend();
         }, "BlockingThread");

         ThreadTools.sleep(200);
         blockingThread.interrupt();

         LogTools.info("Time taken: {}", stopwatch.lapElapsed());
         assertTrue(stopwatch.lapElapsed() > 0.2);
      });
   }
}
