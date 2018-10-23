package us.ihmc.commons;

public class ContinuousIntegrationTools
{
   public static boolean isRunningOnContinuousIntegrationServer()
   {
      String property = System.getProperty("runningOnCIServer");
      String environmentVariable = System.getenv("RUNNING_ON_CONTINUOUS_INTEGRATION_SERVER");

      if (property != null && property.trim().toLowerCase().contains("true"))
      {
         return true;
      }
      else if (environmentVariable != null && environmentVariable.trim().toLowerCase().contains("true"))
      {
         return true;
      }
      else
      {
         return false;
      }
   }
}
