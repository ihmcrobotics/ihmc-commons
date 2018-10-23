package us.ihmc.commons;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.SAME_THREAD)
public class PrintToolsTest
{
   @Test
   public void testPrintTools() throws Exception
   {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      PrintStream systemOut = System.out;

      System.setOut(new PrintStream(byteArrayOutputStream));

      PrintTools.info(this, "Test log tools!");

      System.out.flush();

      System.setOut(systemOut);

      System.out.println("ByteArrayOutputStream.toString(): " + byteArrayOutputStream.toString());

      assertTrue(byteArrayOutputStream.toString().startsWith("[INFO] (PrintToolsTest.java:24): Test log tools!"), "PrintTools didn't work.");
   }

   @Test
   public void testPrintToolsReflection() throws Exception
   {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      PrintStream systemOut = System.out;

      System.setOut(new PrintStream(byteArrayOutputStream));

      PrintTools.info("Test log tools!");

      System.out.flush();

      System.setOut(systemOut);

      System.out.println("ByteArrayOutputStream.toString(): " + byteArrayOutputStream.toString());

      assertTrue(byteArrayOutputStream.toString().startsWith("[INFO] (PrintToolsTest.java:44): Test log tools!"), "PrintTools didn't work.");
   }

   @Test
   public void testPrintToolsError() throws Exception
   {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      PrintStream systemErr = System.err;

      System.setErr(new PrintStream(byteArrayOutputStream));

      PrintTools.error(this, "Test log tools!");

      System.err.flush();

      System.setErr(systemErr);

      System.err.println("ByteArrayOutputStream.toString(): " + byteArrayOutputStream.toString());

      assertTrue(byteArrayOutputStream.toString().startsWith("[ERROR] (PrintToolsTest.java:64): Test log tools!"), "PrintTools didn't work.");
   }

   public static void main(String[] args)
   {
      MutationTestFacilitator.facilitateMutationTestForClass(PrintTools.class, PrintToolsTest.class);
   }
}
