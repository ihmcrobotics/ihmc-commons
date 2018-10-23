package us.ihmc.commons;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

public class AssertionsTest
{
   @Test
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

   @Test
   public void testAssertExceptionNotThrown()
   {
      Assertions.assertExceptionThrown(AssertionFailedError.class, new RunnableThatThrows()
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
