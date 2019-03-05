package us.ihmc.commons.exception;

/**
 * A functional exception handling interface.
 *
 * Can be used in API or tools classes to increase readability in high level code.
 */
@FunctionalInterface
public interface ExceptionHandler
{
   /**
    * Handle an exception.
    *
    * @param e The throwable.
    */
   public void handleException(Throwable e);
}
