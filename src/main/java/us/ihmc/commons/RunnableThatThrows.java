package us.ihmc.commons;

/**
 * Runnable that supports throwables.
 */
public interface RunnableThatThrows
{
   public void run() throws Throwable;
}
