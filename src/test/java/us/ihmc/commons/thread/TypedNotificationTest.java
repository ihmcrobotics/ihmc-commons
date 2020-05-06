package us.ihmc.commons.thread;

import org.junit.jupiter.api.Test;
import us.ihmc.commons.Conversions;

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
}
