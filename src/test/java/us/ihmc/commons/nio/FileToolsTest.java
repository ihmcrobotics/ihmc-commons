package us.ihmc.commons.nio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import us.ihmc.commons.MutationTestFacilitator;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;

public class FileToolsTest
{
   private static final Path FILE_TOOLS_TEST_PATH = getResourcesPathForTestClass(FileToolsTest.class);
   private static final Path TEXT_DIRECTORY_PATH = FILE_TOOLS_TEST_PATH.resolve("exampleTextFiles");
   private static final Path JAVA_DIRECTORY_PATH = FILE_TOOLS_TEST_PATH.resolve("exampleJavaFiles");
   private static final Path EMPTY_DIRECTORY_PATH = FILE_TOOLS_TEST_PATH.resolve("exampleEmptyFiles");
   
   private static final String EXAMPLE_FILE_1_TEXT_LINE_1 = "This is example File 1 !!&&#))(";
   private static final String EXAMPLE_FILE_2_TEXT_LINE_1 = "This is example File 2 *@&&%*@";
   private static final String EXAMPLE_FILE_2_TEXT_LINE_2 = "It has two lines";
   private static final String EXAMPLE_JAVA_FILE1_JAVA = "ExampleJavaFile1.java.txt";
   private static final String EXAMPLE_JAVA_FILE2_JAVA = "ExampleJavaFile2.java.txt";
   private static final String FILE_TOOLS_EXAMPLE_FILE_CAT_TXT = "FileToolsExampleFileCat.txt";
   private static final String FILE_TOOLS_EXAMPLE_FILE1_TXT = "FileToolsExampleFile1.txt";
   private static final String FILE_TOOLS_EXAMPLE_FILE2_TXT = "FileToolsExampleFile2.txt";
   private static final String TEST_READ_ALL_LINES_TXT = "testReadAllLines.txt";
   
   private static final Path EXAMPLE_JAVA_FILE1_PATH = JAVA_DIRECTORY_PATH.resolve(EXAMPLE_JAVA_FILE1_JAVA);
   private static final Path EXAMPLE_JAVA_FILE2_PATH = JAVA_DIRECTORY_PATH.resolve(EXAMPLE_JAVA_FILE2_JAVA);
   private static final Path FILE_TOOLS_EXAMPLE_FILE1_PATH = TEXT_DIRECTORY_PATH.resolve(FILE_TOOLS_EXAMPLE_FILE1_TXT);
   private static final Path FILE_TOOLS_EXAMPLE_FILE2_PATH = TEXT_DIRECTORY_PATH.resolve(FILE_TOOLS_EXAMPLE_FILE2_TXT);
   private static final Path FILE_TOOLS_EXAMPLE_FILE_CAT_TXT_PATH = TEXT_DIRECTORY_PATH.resolve(FILE_TOOLS_EXAMPLE_FILE_CAT_TXT);
   private static final Path READ_ALL_LINES_PATH = FILE_TOOLS_TEST_PATH.resolve(TEST_READ_ALL_LINES_TXT);

   @Before
   public void setUp()
   {
      FileTools.ensureDirectoryExists(FILE_TOOLS_TEST_PATH, DefaultExceptionHandler.PRINT_STACKTRACE);
      FileTools.ensureDirectoryExists(TEXT_DIRECTORY_PATH, DefaultExceptionHandler.PRINT_STACKTRACE);
      FileTools.ensureDirectoryExists(JAVA_DIRECTORY_PATH, DefaultExceptionHandler.PRINT_STACKTRACE);
      FileTools.ensureDirectoryExists(EMPTY_DIRECTORY_PATH, DefaultExceptionHandler.PRINT_STACKTRACE);
      
      createJavaFile1();
      createJavaFile2();
      createTestFile1();
      createTestFile2();
      createReadAllLinesFile();
   }
   
   @After
   public void tearDown()
   {
      FileTools.deleteQuietly(EXAMPLE_JAVA_FILE1_PATH);
      FileTools.deleteQuietly(EXAMPLE_JAVA_FILE2_PATH);
      FileTools.deleteQuietly(FILE_TOOLS_EXAMPLE_FILE1_PATH);
      FileTools.deleteQuietly(FILE_TOOLS_EXAMPLE_FILE2_PATH);
      FileTools.deleteQuietly(FILE_TOOLS_EXAMPLE_FILE_CAT_TXT_PATH);
      FileTools.deleteQuietly(READ_ALL_LINES_PATH);
   }
   
   private static Path getResourcesPathForTestClass(Class<?> clazz)
   {
      List<String> pathNames = new ArrayList<String>();
      
      String[] packageNames = clazz.getPackage().getName().split("\\.");
      
      pathNames.addAll(Arrays.asList(packageNames));
      pathNames.add(StringUtils.uncapitalize(clazz.getSimpleName()));
      
      return Paths.get("testResources", pathNames.toArray(new String[0]));
   }
   
	@ContinuousIntegrationTest(estimatedDuration = 0.0)
   @Test(timeout = 30000)
   public void testReadAllLines()
   {
      List<String> lines = FileTools.readAllLines(READ_ALL_LINES_PATH, DefaultExceptionHandler.PRINT_STACKTRACE);
      
      assertTrue(lines.get(0).equals("line1"));
      assertTrue(lines.get(1).equals("line2"));
      assertTrue(lines.get(2).equals("line3"));
   }
	
	@ContinuousIntegrationTest(estimatedDuration = 0.0)
   @Test(timeout = 30000)
   public void testReadAllBytesAndReadLinesFromBytesAndReplaceLine()
   {
      byte[] bytes = FileTools.readAllBytes(READ_ALL_LINES_PATH, DefaultExceptionHandler.PRINT_STACKTRACE);
      List<String> lines = FileTools.readLinesFromBytes(bytes, DefaultExceptionHandler.PRINT_STACKTRACE);
      
      assertTrue(lines.get(0).equals("line1"));
      assertTrue(lines.get(1).equals("line2"));
      assertTrue(lines.get(2).equals("line3"));
      
      bytes = FileTools.replaceLineInFile(1, "line2Mod", bytes, lines);
      lines = FileTools.readLinesFromBytes(bytes, DefaultExceptionHandler.PRINT_STACKTRACE);

      assertTrue(lines.get(0).equals("line1"));
      assertTrue(lines.get(1).equals("line2Mod"));
      assertTrue(lines.get(2).equals("line3"));
   }

	@ContinuousIntegrationTest(estimatedDuration = 0.0)
   @Test(timeout = 30000)
   public void testConcatenateFilesTogether()
   {      
      Path concatFile1 = FILE_TOOLS_EXAMPLE_FILE1_PATH;
      Path concatFile2 = FILE_TOOLS_EXAMPLE_FILE2_PATH;
      Path concatedFile = TEXT_DIRECTORY_PATH.resolve(FILE_TOOLS_EXAMPLE_FILE_CAT_TXT);

      List<Path> filesToConcat = new ArrayList<Path>();
      filesToConcat.add(concatFile1);
      filesToConcat.add(concatFile2);

      FileTools.concatenateFiles(filesToConcat, concatedFile, DefaultExceptionHandler.PRINT_STACKTRACE);

      try
      {
         BufferedReader reader = Files.newBufferedReader(concatedFile);
         assertEquals(EXAMPLE_FILE_1_TEXT_LINE_1, reader.readLine());
         assertEquals(EXAMPLE_FILE_2_TEXT_LINE_1, reader.readLine());
         assertEquals(EXAMPLE_FILE_2_TEXT_LINE_2, reader.readLine());
         assertNull(reader.readLine());
         reader.close();
      }
      catch (IOException e)
      {
         e.printStackTrace();
         fail();
      }
   }
	
	private static void createTestFile1()
   {
      PrintWriter writer = FileTools.newPrintWriter(FILE_TOOLS_EXAMPLE_FILE1_PATH, WriteOption.TRUNCATE, DefaultExceptionHandler.PRINT_STACKTRACE);
      writer.println(EXAMPLE_FILE_1_TEXT_LINE_1);
      writer.flush();
      writer.close();
   }

   private static void createTestFile2()
   {
      PrintWriter writer = FileTools.newPrintWriter(FILE_TOOLS_EXAMPLE_FILE2_PATH, WriteOption.TRUNCATE, DefaultExceptionHandler.PRINT_STACKTRACE);
      writer.println(EXAMPLE_FILE_2_TEXT_LINE_1);
      writer.println(EXAMPLE_FILE_2_TEXT_LINE_2);
      writer.flush();
      writer.close();
   }

   private static void createJavaFile1()
   {
      PrintWriter writer = FileTools.newPrintWriter(EXAMPLE_JAVA_FILE1_PATH, WriteOption.TRUNCATE, DefaultExceptionHandler.PRINT_STACKTRACE);
      writer.println("// This is a comment");
      writer.println("package us.ihmc.tools.io.files.fileToolsTest.exampleJavaFiles;");
      writer.println("public class ExampleJavaFile1");
      writer.println("{");
      writer.println("public static void main(String[] args)");
      writer.println("{");
      writer.println("System.out.println(\"Hello, World!\");");
      writer.println("}");
      writer.println("}");
      writer.println("// So is this");
      writer.flush();
      writer.close();
   }

   private static void createJavaFile2()
   {
      PrintWriter writer = FileTools.newPrintWriter(EXAMPLE_JAVA_FILE2_PATH, WriteOption.TRUNCATE, DefaultExceptionHandler.PRINT_STACKTRACE);
      writer.println("package us.ihmc.tools.io.files.fileToolsTest.exampleJavaFiles;");
      writer.println("public class ExampleJavaFile2");
      writer.println("{");
      writer.println("public static void main(String[] args)");
      writer.println("{");
      writer.println("System.out.println(\"Hello, World!\");");
      writer.println("}");
      writer.println("}");
      writer.flush();
      writer.close();
   }
   
   private static void createReadAllLinesFile()
   {
      PrintWriter writer = FileTools.newPrintWriter(READ_ALL_LINES_PATH, WriteOption.TRUNCATE, DefaultExceptionHandler.PRINT_STACKTRACE);
      writer.print("line1\r\nline2\nline3\r");
      writer.close();
   }
   
   public static void main(String[] args)
   {
      MutationTestFacilitator.facilitateMutationTestForClass(FileTools.class, FileToolsTest.class);
   }
}
