package us.ihmc.commons.exception;

import us.ihmc.log.LogTools;

/**
 * Create awareness, explicitness, and ease of handling exceptions in default or common ways.
 */
public class DefaultExceptionHandler
{
   /** Does nothing. */
   public static final ExceptionHandler PROCEED_SILENTLY = e -> { };

   /** Prints the stack trace. */
   public static final ExceptionHandler PRINT_STACKTRACE = Throwable::printStackTrace;

   /** Throws a {@link RuntimeException} */
   public static final ExceptionHandler RUNTIME_EXCEPTION = e -> { throw new RuntimeException(e); };

   /** Prints the throwable's message in a friendly way using {@link LogTools} */
   public static final ExceptionHandler PRINT_MESSAGE = e -> LogTools.error(e.getMessage());

   public static final ExceptionHandler MESSAGE_AND_STACKTRACE = e ->
   {
      LogTools.error(e.getMessage());
      e.printStackTrace();
   };

   /** Runs System.exit(1), killing the process and indicating failure. */
   public static final ExceptionHandler KILL_PROCESS = e -> {
      LogTools.error(e.getMessage());
      e.printStackTrace();
      System.exit(1);
   };
}
