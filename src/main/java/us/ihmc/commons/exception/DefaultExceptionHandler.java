package us.ihmc.commons.exception;

import us.ihmc.commons.PrintTools;

/**
 * Create awareness, explicitness, and ease of handling exceptions in default or common ways.
 */
public class DefaultExceptionHandler
{
   /** Does nothing. */
   public static ExceptionHandler PROCEED_SILENTLY = e -> { };

   /** Runs System.exit(1), killing the process and indicating failure. */
   public static ExceptionHandler KILL_PROCESS = e -> {
      PrintTools.error(DefaultExceptionHandler.class, e.getMessage());
      System.exit(1);
   };

   /** Prints the stack trace. */
   public static ExceptionHandler PRINT_STACKTRACE = Throwable::printStackTrace;

   /** Throws a {@link RuntimeException} */
   public static ExceptionHandler RUNTIME_EXCEPTION = e -> { throw new RuntimeException(e); };

   /** Prints the throwable's message in a friendly way using {@link PrintTools} */
   public static ExceptionHandler PRINT_MESSAGE = e -> PrintTools.error(DefaultExceptionHandler.class, e.getMessage());
}
