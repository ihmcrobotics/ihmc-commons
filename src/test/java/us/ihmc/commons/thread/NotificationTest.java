package us.ihmc.commons.thread;

import org.junit.jupiter.api.Test;
import us.ihmc.commons.Conversions;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationTest
{
   @Test
   public void testNotified()
   {
      Notification notification = new Notification();

      assertFalse(notification.poll());
      assertFalse(notification.read());

      notification.set();

      assertTrue(notification.poll());
      assertTrue(notification.read());
   }

   @Test
   public void testNotifiedBeforeBlockingPollCalled()
   {
      Notification notification = new Notification();

      notification.set();
      assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
      {
         notification.blockingPoll();
      });

      assertTrue(notification.read());
   }

   @Test
   public void testNotificationFromThread()
   {
      assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
      {
         Notification notification = new Notification();

         assertFalse(notification.poll());
         assertFalse(notification.read());

         long before = System.nanoTime();
         ThreadTools.startAThread(() ->
         {
            ThreadTools.sleep(200);
            notification.set();
         }, "SetterThread");

         notification.blockingPoll();

         long after = System.nanoTime();

         assertTrue(Conversions.nanosecondsToMilliseconds(after - before) > 200);

         assertTrue(notification.read());
      });
   }
}
