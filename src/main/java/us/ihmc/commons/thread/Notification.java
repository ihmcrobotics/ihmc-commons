package us.ihmc.commons.thread;

import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.exception.ExceptionTools;

/**
 * <p>Provides a simple interface for setting a bit from one thread
 * and clearing it from another in acknowledgement. Stores the last
 * polled state for re-access, since the first poll clears the bit.</p><br>
 *
 * <p>This class exists to provide convenience and prevent a common
 * bug, that is, forgetting to clear the bit after a poll.</p><br>
 *
 * <p>The usefulness is illustrated by the following example:</p>
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
   private boolean previousValue = false;

   /**
    * Polls and clears the notification.
    *
    * @return if notification was set
    */
   public synchronized boolean poll()
   {
      previousValue = notification;
      notification = false;
      return previousValue;
   }

   /**
    * Block and wait to be notified.
    *
    * @return notification
    */
   public synchronized boolean blockingPoll()
   {
      ExceptionTools.handle(() -> this.wait(), DefaultExceptionHandler.RUNTIME_EXCEPTION);
      return poll();
   }

   /**
    * If on the last poll the notification was set. Should be called after {@link #poll}
    * for convenience, as many times as you like.
    *
    * If this notification has never been polled, returns the initial value, false.
    *
    * @return if on the last poll the notification was set
    */
   public boolean read()
   {
      return previousValue;
   }

   /** THREAD 2 ACCESS BELOW THIS POINT TODO: Make this safe somehow? Store thread names? */

   /**
    * Sets the notification.
    */
   public synchronized void set()
   {
      notification = true;

      this.notifyAll(); // if wait has been called, notify it
   }
}