package us.ihmc.commons.exception;

import us.ihmc.commons.RunnableThatThrows;

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
    * @param genericRunnable
    * @param exceptionHandler
    */
   public static <T> T handle(GenericRunnable<T> genericRunnable, ExceptionHandler exceptionHandler)
   {
      try
      {
         return genericRunnable.run();
      }
      catch (Throwable e)
      {
         exceptionHandler.handleException(e);
         return null;
      }
   }
}
