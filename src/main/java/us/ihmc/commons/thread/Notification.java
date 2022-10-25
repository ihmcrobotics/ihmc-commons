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
 * private final Notification callbackHappened = new Notification();
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
    * Peeks at the value of the notification without clearing it.
    *
    * @return if the notification has been set
    */
   public boolean peek()
   {
      return notification;
   }

   /**
    * If notification not already set, block and wait to be notified.
    * Does not clear this notification.
    */
   public synchronized void blockingPeek()
   {
      if (!peek())
      {
         ExceptionTools.handle(() -> this.wait(), DefaultExceptionHandler.RUNTIME_EXCEPTION);
         peek();
      }
   }

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
    * Clears the notification.
    */
   public synchronized void clear()
   {
      previousValue = notification;
      notification = false;
   }

   /**
    * If notification not already set, block and wait to be notified.
    * Clears this notification.
    */
   public synchronized void blockingPoll()
   {
      blockingPeek();
      poll();
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