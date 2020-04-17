package us.ihmc.commons.thread;

import java.util.concurrent.ThreadFactory;

/**
 * @deprecated Use {@link ThreadTools} createNamedThreadFactory methods
 */
public class DaemonThreadFactory
{
   private DaemonThreadFactory()
   {
      // disallow construction
   }

   /**
    * @deprecated Use {@link ThreadTools#createNamedDaemonThreadFactory(String)} instead
    */
   public static ThreadFactory getNamedDaemonThreadFactory(String name)
   {
      ThreadFactory threadFactory = ThreadTools.getNamedThreadFactory(name);

      return runnable ->
      {
         Thread newThread = threadFactory.newThread(runnable);
         newThread.setDaemon(true);
         return newThread;
      };
   }
}