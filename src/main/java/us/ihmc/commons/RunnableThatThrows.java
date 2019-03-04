package us.ihmc.commons;

/**
 * Runnable that supports throwables.
 */
@FunctionalInterface
public interface RunnableThatThrows
{
   void run() throws Throwable;
}
