package us.ihmc.commons;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertTrue;

public class PrintToolsTest
{
   @Test(timeout = 30000)
   public void testPrintTools() throws Exception
   {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      PrintStream systemOut = System.out;

      System.setOut(new PrintStream(byteArrayOutputStream));

      PrintTools.info(this, "Test log tools!");

      System.out.flush();

      System.setOut(systemOut);

      System.out.println("ByteArrayOutputStream.toString(): " + byteArrayOutputStream.toString());

      assertTrue("PrintTools didn't work.", byteArrayOutputStream.toString().startsWith("[INFO] (PrintToolsTest.java:21): Test log tools!"));
   }

   @Test(timeout = 30000)
   public void testPrintToolsReflection() throws Exception
   {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      PrintStream systemOut = System.out;

      System.setOut(new PrintStream(byteArrayOutputStream));

      PrintTools.info("Test log tools!");

      System.out.flush();

      System.setOut(systemOut);

      System.out.println("ByteArrayOutputStream.toString(): " + byteArrayOutputStream.toString());

      assertTrue("PrintTools didn't work.", byteArrayOutputStream.toString().startsWith("[INFO] (PrintToolsTest.java:41): Test log tools!"));
   }

   public static void main(String[] args)
   {
      MutationTestFacilitator.facilitateMutationTestForClass(PrintTools.class, PrintToolsTest.class);
   }
}
