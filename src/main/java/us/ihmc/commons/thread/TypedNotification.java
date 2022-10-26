package us.ihmc.commons.thread;

import us.ihmc.commons.RunnableThatThrows;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.exception.ExceptionHandler;
import us.ihmc.commons.exception.ExceptionTools;

/**
 * A thread barrier over an object. An external thread notifies the holder with an Object.
 * The holder polls for notifications, with the option to block with #blockingPoll. Upon
 * polling a notification, it is cleared. After a poll, the value may be read later as
 * much as desired with #read.
 * <p/>
 * The Notification classes are to pass data to another thread. Sort of like a size 1 concurrent buffer.
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
    * @return if the notification has been set
    */
   public T peek()
   {
      return notification;
   }

   /**
    * Get the atomic value, store it for a later call to read, and return if new value was present.
    *
    * @return value available
    */
   public synchronized boolean poll()
   {
      previousValue = notification;
      notification = null;
      return previousValue != null;
   }

   /**
    * If value not immediately available, block and wait to be notified.
    *
    * If interrupted, throw RuntimeException.
    *
    * @return notification
    */
   public T blockingPoll()
   {
      return blockingPoll(DefaultExceptionHandler.RUNTIME_EXCEPTION);
   }

   /**
    * If value not immediately available, block and wait to be notified.
    *
    * @param exceptionHandler Handle interrupted exception
    * @return notification
    */
   public synchronized T blockingPoll(ExceptionHandler exceptionHandler)
   {
      if (!poll())
      {
         ExceptionTools.handle((RunnableThatThrows) this::wait, exceptionHandler);
         poll();
      }
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
    * @return polled value
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
