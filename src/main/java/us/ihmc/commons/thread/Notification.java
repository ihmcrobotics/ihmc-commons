package us.ihmc.commons.thread;

/**
 * <p>Provides a simple interface for setting a bit from one thread
 * and clearing it from another in acknowledgement. Additionally
 * provides one frame of history for convenience.</p><br>
 *
 * <p>This class exists to provide convenience and prevent a common
 * bug, that is, forgetting to clear the bit after a poll.</p><br>
 *
 * <p>The API difference is illustrated by the following example:</p>
 *
 * <pre>
 * {@code
 * private final NotificationReference callbackHappened = new NotificationReference();
 *
 * public void handle(long now)
 * {
 *    if (callbackHappened.poll())  // do stuff
 * }
 *
 * private void someCallback()
 * {
 *    callbackHappened.set();
 * }
 * }
 * </pre>
 *
 * as opposed to
 *
 * <pre>
 * {@code
 * private boolean callbackHappened = false;
 *
 * public void handle(long now)
 * {
 *    if (callbackHappened)  callbackHappened = false; // do stuff     <-- This is a bug waiting to happen
 * }
 *
 * private void someCallback()
 * {
 *    callbackHappened = true;
 * }
 * }
 * </pre>
 */
public class Notification
{
   private boolean notification = false;

   /**
    * Polls and clears the notification.
    *
    * @return if notification was present
    */
   public boolean poll()
   {
      boolean previousValue = notification;
      notification = false;
      return previousValue;
   }

   /**
    * Sets the notification.
    */
   public void set()
   {
      notification = true;
   }
}