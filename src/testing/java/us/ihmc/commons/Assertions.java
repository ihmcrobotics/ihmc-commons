package us.ihmc.commons;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class Assertions
{
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
