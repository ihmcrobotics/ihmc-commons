package us.ihmc.commons.thread;

import us.ihmc.commons.Conversions;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.exception.ExceptionHandler;
import us.ihmc.commons.exception.ExceptionTools;
import us.ihmc.log.LogTools;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This class first prints any unchecked exception messages and termination message so that it is impossible to miss.
 *
 * It also returns a ScheduledFuture so the exception may be handled, but meant it could no longer implement PeriodicThreadScheduler.
 */
public class ExceptionHandlingThreadScheduler
{
   public static final ExceptionHandler DEFAULT_HANDLER = t ->
   {
      LogTools.error(t.getMessage());
      t.printStackTrace();
      LogTools.error("{} is terminating due to an exception.", Thread.currentThread().getName());
   };

   private final ScheduledExecutorService executorService;
   private final ExceptionHandler exceptionHandler;
   private final long permissableNumberOfExceptions;
   private long numberOfExceptionsThrown = 0;
   private Runnable runnable;
   private ScheduledFuture<?> scheduledFuture;
   private volatile boolean isRunningTask = false;

   /**
    * Normal operation. Just print a good error message and shutdown.
    *
    * @param name thread name
    */
   public ExceptionHandlingThreadScheduler(String name)
   {
      this(name, null, 0);
   }

   /**
    * Handle the exceptions yourself and recover. Always resume running.
    *
    * @param name thread name
    * @param exceptionHandler
    */
   public ExceptionHandlingThreadScheduler(String name, ExceptionHandler exceptionHandler)
   {
      this(name, exceptionHandler, -1);
   }

   /**
    * Try to handle the exception but give up after N tries.
    *
    * @param prefix thread name prefix
    * @param exceptionHandler
    * @param permissableNumberOfExceptions allow this many exceptions, then terminate on the next one. Pass -1 to never terminate.
    */
   public ExceptionHandlingThreadScheduler(String prefix, ExceptionHandler exceptionHandler, long permissableNumberOfExceptions)
   {
      this(prefix, exceptionHandler, permissableNumberOfExceptions, false);
   }

   /**
    * Try to handle the exception but give up after N tries. Also allows to run the thread as a daemon.
    *
    * @param prefix thread name prefix
    * @param exceptionHandler
    * @param permissableNumberOfExceptions allow this many exceptions, then terminate on the next one. Pass 0 to never terminate.
    */
   public ExceptionHandlingThreadScheduler(String prefix, ExceptionHandler exceptionHandler, long permissableNumberOfExceptions, boolean runAsDaemon)
   {
      executorService = runAsDaemon ? ThreadTools.newSingleDaemonThreadScheduledExecutor(prefix) : ThreadTools.newSingleThreadScheduledExecutor(prefix);
      this.exceptionHandler = exceptionHandler;
      this.permissableNumberOfExceptions = permissableNumberOfExceptions;
   }

   public ScheduledFuture<?> schedule(Runnable runnable, double period)
   {
      return schedule(runnable, Conversions.secondsToNanoseconds(period), TimeUnit.NANOSECONDS);
   }

   public ScheduledFuture<?> schedule(Runnable runnable, long period, TimeUnit timeunit)
   {
      this.runnable = runnable;
      ExceptionTools.handle(() -> scheduledFuture = executorService.scheduleAtFixedRate(this::printingRunnableWrapper, 0, period, timeunit),
                            exception -> LogTools.error(exception.getMessage()));  // reduce error output to just a message

      return scheduledFuture;
   }

   public ScheduledFuture<?> scheduleOnce(Runnable runnable)
   {
      this.runnable = runnable;
      ExceptionTools.handle(() -> scheduledFuture = executorService.schedule(this::printingRunnableWrapper, 0, TimeUnit.MILLISECONDS),
                            exception -> LogTools.error(exception.getMessage()));  // reduce error output to just a message

      return scheduledFuture;
   }

   private void printingRunnableWrapper()
   {
      isRunningTask = true;
      try
      {
         runnable.run();
      }
      catch (Exception e)
      {
         if (permissableNumberOfExceptions > -1)
         {
            ++numberOfExceptionsThrown;
            if (hasSurpassedPermissableExceptions())
            {
               if (exceptionHandler == null)
               {
                  LogTools.error("{} is terminating due to reaching {} exceptions.", Thread.currentThread().getName(), numberOfExceptionsThrown);

                  // We need to throw a RuntimeException because regular exceptions happen silently.
                  throw new RuntimeException(e);
               }
               else
               {
                  exceptionHandler.handleException(e);
               }
            }
         }
         else
         {
            if (exceptionHandler == null)
            {
               DefaultExceptionHandler.MESSAGE_AND_STACKTRACE.handleException(e);
            }
            else
            {
               exceptionHandler.handleException(e);
            }
         }
      }
      finally
      {
         isRunningTask = false;
      }
   }

   public void shutdown()
   {
      executorService.shutdown();
   }

   public void shutdownNow()
   {
      executorService.shutdownNow();
   }

   public boolean isRunningTask()
   {
      return isRunningTask;
   }

   public boolean hasSurpassedPermissableExceptions()
   {
      return numberOfExceptionsThrown > permissableNumberOfExceptions;
   }
}