package us.ihmc.commons.exception;

import us.ihmc.commons.RunnableThatThrows;

import java.util.concurrent.Callable;

public class ExceptionTools
{
   /**
    * One-liner exception handling when used with {@link DefaultExceptionHandler}.
    *
    * @param runnable
    * @param exceptionHandler
    */
   public static void handle(RunnableThatThrows runnable, ExceptionHandler exceptionHandler)
   {
      try
      {
         runnable.run();
      }
      catch (Throwable e)
      {
         exceptionHandler.handleException(e);
      }
   }

   /**
    * One-liner exception handling when used with {@link DefaultExceptionHandler}.
    * Also returns a value.
    *
    * @param callable
    * @param exceptionHandler
    */
   public static <T> T handle(Callable<T> callable, ExceptionHandler exceptionHandler)
   {
      try
      {
         return callable.call();
      }
      catch (Exception e)
      {
         exceptionHandler.handleException(e);
         return null;
      }
   }
}
