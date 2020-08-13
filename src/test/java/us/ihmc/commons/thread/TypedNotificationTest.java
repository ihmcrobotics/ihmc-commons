package us.ihmc.commons.thread;

import org.junit.jupiter.api.Test;
import us.ihmc.commons.Conversions;
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

      assertFalse(notification.poll());
      assertNull(notification.read());

      notification.set(6);

      assertTrue(notification.poll());
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
         TypedNotification<Integer> notification = new TypedNotification();

         assertFalse(notification.poll());
         assertEquals(null, notification.read());

         long before = System.nanoTime();
         ThreadTools.startAThread(() ->
                                  {
                                     ThreadTools.sleep(200);
                                     notification.set(8);
                                  }, "SetterThread");

         assertEquals(8, notification.blockingPoll());

         long after = System.nanoTime();

         assertTrue(Conversions.nanosecondsToMilliseconds(after - before) > 200);

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
