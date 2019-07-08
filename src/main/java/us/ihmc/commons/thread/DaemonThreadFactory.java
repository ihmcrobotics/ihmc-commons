package us.ihmc.commons.thread;

import java.util.concurrent.ThreadFactory;

public class DaemonThreadFactory
{
   private DaemonThreadFactory()
   {
      // disallow construction
   }

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