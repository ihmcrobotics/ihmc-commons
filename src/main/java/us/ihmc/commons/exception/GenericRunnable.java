package us.ihmc.commons.exception;

/**
 * Runnable that throws and returns.
 */
@FunctionalInterface
public interface GenericRunnable<T>
{
   T run() throws Throwable;
}
