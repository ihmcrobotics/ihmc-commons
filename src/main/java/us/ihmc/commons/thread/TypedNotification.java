package us.ihmc.commons.thread;

import us.ihmc.commons.RunnableThatThrows;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.exception.ExceptionHandler;
import us.ihmc.commons.exception.ExceptionTools;

/**
 * <p>TypedNotification is used to pass data between threads. It is like a concurrent buffer of size 1.
 * An external thread notifies the holder with an Object using {@link TypedNotification#set}.
 * The holder polls for notifications, with the option to block with {@link TypedNotification#blockingPoll}. Upon
 * polling a notification, it is cleared. After a poll, the value may be read later as
 * much as desired with {@link TypedNotification#read}.</p>
 *
 * <p>This class also provides the corresponding {@link TypedNotification#peek} methods, which can be used
 * to check the value of the notification without clearing it.</p>
 *
 * <p>Note that null is used to determine whether the notification is set. Setting the Notification
 * to null will not have an effect. Similarly, the hasValue methods are just a null check.</p>
 */
public class TypedNotification<T>
{
   private volatile T notification = null;
   private T previousValue = null;

   /**
    * Peeks at the value of the notification without clearing it to see
    * if this notification has been set.
    *
    * @return if the notification has been set
    */
   public boolean peekHasValue()
   {
      return notification != null;
   }

   /**
    * Peeks at the value of the notification without clearing it.
    *
    * @return value of the notification or null if it is not set
    */
   public T peek()
   {
      return notification;
   }

   /**
    * If value not immediately available, block and wait to be notified.
    * Does not clear this notification.
    *
    * If interrupted, throw RuntimeException.
    *
    * @return value of the notification or null if it is not set
    */
   public synchronized T blockingPeek()
   {
      return blockingPeek(DefaultExceptionHandler.RUNTIME_EXCEPTION);
   }

   /**
    * If value not immediately available, block and wait to be notified.
    * Does not clear this notification.
    *
    * @param exceptionHandler Handle interrupted exception
    * @return value of the notification or null if it is not set
    */
   public synchronized T blockingPeek(ExceptionHandler exceptionHandler)
   {
      if (!peekHasValue())
      {
         ExceptionTools.handle((RunnableThatThrows) this::wait, exceptionHandler);
      }
      return notification;
   }

   /**
    * Get the atomic value, store it for a later call to read, and return if new value was present.
    *
    * @return if notification was set and a value is available
    */
   public synchronized boolean poll()
   {
      previousValue = notification;
      notification = null;
      return previousValue != null;
   }

   /**
    * If value not immediately available, block and wait to be notified.
    * Clears this notification.
    *
    * If interrupted, throw RuntimeException.
    *
    * @return polled value or null if the notification was not set
    */
   public T blockingPoll()
   {
      return blockingPoll(DefaultExceptionHandler.RUNTIME_EXCEPTION);
   }

   /**
    * If value not immediately available, block and wait to be notified.
    * Clears this notification.
    *
    * @param exceptionHandler Handle interrupted exception
    * @return polled value or null if the notification was not set
    */
   public synchronized T blockingPoll(ExceptionHandler exceptionHandler)
   {
      blockingPeek(exceptionHandler);
      poll();
      return previousValue;
   }

   /**
    * If on the last poll the notification was set. Should be called after {@link #poll}
    * for convenience, as many times as you like.
    *
    * If this notification has never been polled, returns the initial value, false.
    *
    * @return if on the last poll the notification was set
    */
   public boolean hasValue()
   {
      return previousValue != null;
   }

   /**
    * The initial or polled value.
    * <p/>
    * Must have called {@link #poll()} first!
    *
    * @return polled value or null if the notification was not set
    */
   public T read()
   {
      return previousValue;
   }

   /** THREAD 2 ACCESS BELOW THIS POINT TODO: Make this safe somehow? Store thread names? */

   /**
    * Sets the notification and triggers notifyAll().
    *
    * @param value
    */
   public synchronized void set(T value)
   {
      notification = value;

      this.notifyAll(); // if wait has been called, notify it
   }
}
