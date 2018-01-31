package us.ihmc.commons;

import java.util.logging.Logger;

public class LogToolsTest
{
   private static final Logger logger = LogTools.getLogger(LogToolsTest.class);

   public static void main(String[] args)
   {
      LogTools.severe("erororor");

      LogTools.warning("warning bro");

      LogTools.info("infographic");

      LogTools.config("configg");

      LogTools.fine("fineee");

      LogTools.finer("more finnneee");

      LogTools.finest("absolute finest");
   }
}
