package us.ihmc.commons.thread;

import org.junit.jupiter.api.Test;
import us.ihmc.commons.Conversions;
import us.ihmc.log.LogTools;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationTest
{
   @Test
   public void testNotified()
   {
      Notification notification = new Notification();

      assertFalse(notification.peek());
      assertFalse(notification.poll());
      assertFalse(notification.peek());
      assertFalse(notification.peek());
      assertFalse(notification.read());
      assertFalse(notification.read());
      assertFalse(notification.peek());
      assertFalse(notification.read());

      notification.set();

      assertTrue(notification.peek());
      assertTrue(notification.peek());
      assertTrue(notification.poll());
      assertFalse(notification.peek());
      assertFalse(notification.peek());
      assertTrue(notification.read());
      assertTrue(notification.read());
      assertFalse(notification.peek());
      assertTrue(notification.read());
      assertFalse(notification.poll());
      assertFalse(notification.peek());
      assertFalse(notification.peek());
      assertFalse(notification.read());
      assertFalse(notification.read());
      assertFalse(notification.peek());
      assertFalse(notification.read());

      notification.set();

      assertTrue(notification.peek());
      assertTrue(notification.poll());

      notification.set();
      notification.set();

      assertTrue(notification.peek());
      assertTrue(notification.poll());
   }

   @Test
   public void testClear()
   {
      Notification notification = new Notification();

      assertFalse(notification.poll());
      assertFalse(notification.read());

      notification.set();

      assertTrue(notification.poll());
      assertTrue(notification.read());

      notification.set();

      notification.clear();
      assertFalse(notification.poll());
      assertFalse(notification.read());
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
   public void testNotifiedBeforeBlockingPeekCalled()
   {
      Notification notification = new Notification();

      notification.set();
      assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
      {
         notification.blockingPeek();
      });

      assertTrue(notification.peek());
      assertFalse(notification.read());
      assertTrue(notification.poll());
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

         assertTrue(Conversions.nanosecondsToMilliseconds(after - before) >= 200);

         assertTrue(notification.read());
      });

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

         notification.blockingPeek();

         long after = System.nanoTime();

         boolean condition = Conversions.nanosecondsToMilliseconds(after - before) >= 200;
         if (!condition)
         {
            LogTools.info("Time taken: {}", Conversions.nanosecondsToMilliseconds(after - before));
         }
         assertTrue(condition);

         assertTrue(notification.peek());
         assertFalse(notification.read());
         assertTrue(notification.poll());
         assertTrue(notification.read());
      });
   }
}
