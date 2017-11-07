package us.ihmc.commons;

import org.junit.Test;

import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;

public class AssertionsTest
{
   @ContinuousIntegrationTest(estimatedDuration = 0.0)
   @Test(timeout = 30000)
   public void testAssertExceptionThrown()
   {
      Assertions.assertExceptionThrown(Exception.class, new RunnableThatThrows()
      {
         @Override
         public void run() throws Throwable
         {
            throw new Exception();
         }
      });
   }

   @ContinuousIntegrationTest(estimatedDuration = 0.0)
   @Test(timeout = 30000)
   public void testAssertExceptionNotThrown()
   {
      Assertions.assertExceptionThrown(AssertionError.class, new RunnableThatThrows()
      {
         @Override
         public void run() throws Throwable
         {
            Assertions.assertExceptionThrown(IndexOutOfBoundsException.class, new RunnableThatThrows()
            {
               @Override
               public void run() throws Throwable
               {
                  throw new NullPointerException();
               }
            });
         }
      });
   }
}
