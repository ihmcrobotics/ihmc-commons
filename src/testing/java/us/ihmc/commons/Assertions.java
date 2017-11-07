package us.ihmc.commons;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Assertions
{
   /**
    * Assert that an exception is thrown given a runnable that throws.
    * 
    * @param exceptionType
    * @param methodToRun
    */
   public static void assertExceptionThrown(Class<? extends Throwable> exceptionType, RunnableThatThrows methodToRun)
   {
      boolean thrown = false;

      try
      {
         methodToRun.run();
      }
      catch (Throwable throwable)
      {
         assertTrue("Exception type mismatch: Expected: " + exceptionType.getName() + " Actual: " + throwable.getClass().getName(),
                    exceptionType.getName().equals(throwable.getClass().getName()));

         thrown = true;
      }

      assertTrue("Exception not thrown", thrown);
   }

   /**
    * <p>Assert an object is serializable by actually serializing.</p>
    * 
    * <p>Does not assert equal.</p>
    * 
    * @param serializable
    */
   public static void assertSerializable(Serializable serializable)
   {
      assertSerializable(serializable, false);
   }

   /**
    * Assert an object is serializable by actually serializing and optionally
    * asserting equals.
    * 
    * @param serializable
    * @param assertEqual
    */
   public static void assertSerializable(Serializable serializable, boolean assertEqual)
   {
      try
      {
         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
         objectOutputStream.writeObject(serializable);

         ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
         ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
         Object object = objectInputStream.readObject();
         if (assertEqual)
         {
            assertTrue(object.equals(serializable));
         }
      }
      catch (IOException e)
      {
         fail("Object not serializable");
      }
      catch (ClassNotFoundException ce)
      {
         fail("Object not serializable: class not found");
      }
   }
}
