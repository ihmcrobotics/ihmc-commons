package us.ihmc.commons.nio;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import us.ihmc.commons.MutationTestFacilitator;
import us.ihmc.log.LogTools;
import us.ihmc.commons.exception.DefaultExceptionHandler;

import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PathToolsTest
{
   private static final Path PARENT_DIRECTORY = Paths.get("testDirectory2");
   private static final Path FAKE_JAVA_FILE_PATH = createFakeJavaPath();
   private static final Path[] TEST_DIRECTORIES = {PARENT_DIRECTORY.resolve("testDir1"), PARENT_DIRECTORY.resolve("testDir2")};
   private static final Path[] TEST_FILES = {TEST_DIRECTORIES[0].resolve("testFile1.txt"), TEST_DIRECTORIES[1].resolve("testFile2.txt")};

   @BeforeEach
   public void setUp()
   {
      FileTools.ensureFileExists(TEST_FILES[0], DefaultExceptionHandler.PRINT_STACKTRACE);
      FileTools.ensureFileExists(TEST_FILES[1], DefaultExceptionHandler.PRINT_STACKTRACE);

      FileTools.ensureFileExists(FAKE_JAVA_FILE_PATH, DefaultExceptionHandler.PRINT_STACKTRACE);
      FileTools.writeAllLines(createFakeJavaFile(), FAKE_JAVA_FILE_PATH, WriteOption.TRUNCATE, DefaultExceptionHandler.PRINT_STACKTRACE);
   }

   @AfterEach
   public void tearDown()
   {
      FileTools.deleteQuietly(PARENT_DIRECTORY);
      FileTools.deleteQuietly(FAKE_JAVA_FILE_PATH);
   }

   @Test
   public void testFindAllPathsRecursivelyThatMatchRegex()
   {
      List<Path> matchingPaths = PathTools.findAllPathsRecursivelyThatMatchRegex(Paths.get("resources"), ".*[\\\\/]PathTools\\.java\\.fake$");

      LogTools.info("Matched " + matchingPaths.size() + " file(s).");

      assertTrue(matchingPaths.size() == 1, "Didn't match exactly one file.");
   }

   @Test
   public void testGetBaseName()
   {
      Path pathToThisTest = FileSystems.getDefault().getPath("test/us/ihmc/utilities/io/files/PathToolsTest.java");

      String baseName = PathTools.getBaseName(pathToThisTest);

      LogTools.info("Base name of this test: " + baseName);

      assertTrue(baseName.equals(PathToolsTest.class.getSimpleName()), "Base name not correct.");
   }

   @Test
   public void testGetExtension()
   {
      Path pathToThisTest = FileSystems.getDefault().getPath("test/us/ihmc/utilities/io/files/PathToolsTest.java");

      String extensionName = PathTools.getExtension(pathToThisTest);

      LogTools.info("Extension name of this test: " + extensionName);

      assertTrue(extensionName.equals("java"), "Extension name not correct.");
   }

   @Test
   public void testFirstPathMatchingGlob()
   {
      String camelCasedClassSimpleName = StringUtils.uncapitalize(PathToolsTest.class.getSimpleName());

      LogTools.info("Camel cased simple name: " + camelCasedClassSimpleName);

      Path firstPath = PathTools.findFirstPathMatchingGlob(Paths.get("resources"), "**/" + camelCasedClassSimpleName);

      if (firstPath == null)
         LogTools.error("Path not found!");

      assertTrue(PathTools.directoryHasGlob(Paths.get("resources"), "**/" + camelCasedClassSimpleName), "directoryHasGlob not working.");

      LogTools.info("First path: " + firstPath.toString());
      LogTools.info("First path fileName: " + firstPath.getFileName());

      assertTrue(firstPath.getFileName().toString().equals(camelCasedClassSimpleName), "First path not correct.");
   }

   @Test
   public void testTemporaryDirectoryPath()
   {
      String tempPath = PathTools.systemTemporaryDirectory().toString();
      LogTools.info("Java temp directory: " + tempPath);
      assertNotNull("Java temp directory is null.", tempPath);
   }

   @Test
   public void testWalkTreeFlat()
   {
      PathTools.walkFlat(PARENT_DIRECTORY, new BasicPathVisitor()
      {
         int resultCount = 0;

         @Override
         public FileVisitResult visitPath(Path path, PathType pathType)
         {
            LogTools.info(path.toString() + " " + pathType.toString());
            if (path.equals(TEST_DIRECTORIES[0]) || path.equals(TEST_DIRECTORIES[1]))
            {
               assertTrue(pathType.equals(PathType.DIRECTORY), "Falsely reported directory");
            }
            else if (path.equals(TEST_FILES[0]) || path.equals(TEST_FILES[1]))
            {
               assertTrue(pathType.equals(PathType.FILE), "Falsely reported file");
            }

            resultCount++;

            assertTrue(resultCount <= 4, "Parent was included");

            return FileVisitResult.CONTINUE;
         }
      });
   }

   private static final Path createFakeJavaPath()
   {
      String[] split = PathToolsTest.class.getPackage().getName().split("\\.");
      List<String> packageParts = new ArrayList<>();
      packageParts.add("resources");
      packageParts.addAll(Arrays.asList(split));
      packageParts.add(StringUtils.uncapitalize(PathToolsTest.class.getSimpleName()));
      packageParts.add(PathToolsTest.class.getSimpleName() + ".java.fake");
      Path path = Paths.get("");
      for (String part : packageParts)
      {
         path = path.resolve(part);
         System.out.println(path);
      }
      return path;
   }

   private static final List<String> createFakeJavaFile()
   {
      ArrayList<String> fakeJavaFile = new ArrayList<>();
      fakeJavaFile.add("package us.ihmc.tools.io.files;");
      fakeJavaFile.add("");
      fakeJavaFile.add("import java.io.IOException;");
      fakeJavaFile.add("import java.nio.file.FileSystems;");
      fakeJavaFile.add("import java.nio.file.FileVisitOption;");
      fakeJavaFile.add("import java.nio.file.FileVisitResult;");
      fakeJavaFile.add("import java.nio.file.Files;");
      fakeJavaFile.add("import java.nio.file.Path;");
      fakeJavaFile.add("import java.nio.file.PathMatcher;");
      fakeJavaFile.add("import java.nio.file.SimpleFileVisitor;");
      fakeJavaFile.add("import java.nio.file.attribute.BasicFileAttributes;");
      fakeJavaFile.add("import java.util.ArrayList;");
      fakeJavaFile.add("import java.util.EnumSet;");
      fakeJavaFile.add("import java.util.List;");
      fakeJavaFile.add("");
      fakeJavaFile.add("import org.apache.commons.io.FilenameUtils;");
      fakeJavaFile.add("");
      fakeJavaFile.add("import us.ihmc.tools.io.files.BasicPathVisitor.PathType;");
      fakeJavaFile.add("");
      fakeJavaFile.add("public class PathTools");
      fakeJavaFile.add("{");
      fakeJavaFile.add("   public static final String GLOB_SYNTAX_PREFIX = \"glob:\";");
      fakeJavaFile.add("   public static final String REGEX_SYNTAX_PREFIX = \"regex:\";");
      fakeJavaFile.add("   ");
      fakeJavaFile.add("   public static List<Path> findAllPathsRecursivelyThatMatchRegex(Path rootPath, String regex)");
      fakeJavaFile.add("   {");
      fakeJavaFile.add("      final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(REGEX_SYNTAX_PREFIX + regex);");
      fakeJavaFile.add("      final List<Path> matchingPaths = new ArrayList<Path>();");
      fakeJavaFile.add("      ");
      fakeJavaFile.add("      walkRecursively(rootPath, new BasicPathVisitor()");
      fakeJavaFile.add("      {");
      fakeJavaFile.add("         @Override");
      fakeJavaFile.add("         public FileVisitResult visitPath(Path path, PathType pathType)");
      fakeJavaFile.add("         {");
      fakeJavaFile.add("            if (matcher.matches(path))");
      fakeJavaFile.add("               matchingPaths.add(path);");
      fakeJavaFile.add("            ");
      fakeJavaFile.add("            return FileVisitResult.CONTINUE;");
      fakeJavaFile.add("         }");
      fakeJavaFile.add("      });");
      fakeJavaFile.add("      ");
      fakeJavaFile.add("      return matchingPaths;");
      fakeJavaFile.add("   }");
      fakeJavaFile.add("   ");
      fakeJavaFile.add("   public static boolean contains(Path path, String name)");
      fakeJavaFile.add("   {");
      fakeJavaFile.add("      for (int i = 0; i < path.getNameCount(); i++)");
      fakeJavaFile.add("      {");
      fakeJavaFile.add("         if (path.getName(i).toString().equals(name))");
      fakeJavaFile.add("         {");
      fakeJavaFile.add("            return true;");
      fakeJavaFile.add("         }");
      fakeJavaFile.add("      }");
      fakeJavaFile.add("");
      fakeJavaFile.add("      return false;");
      fakeJavaFile.add("   }");
      fakeJavaFile.add("");
      fakeJavaFile.add("   public static String getBaseName(Path path)");
      fakeJavaFile.add("   {");
      fakeJavaFile.add("      return FilenameUtils.getBaseName(path.toString());");
      fakeJavaFile.add("   }");
      fakeJavaFile.add("   ");
      fakeJavaFile.add("   public static String getExtension(Path path)");
      fakeJavaFile.add("   {");
      fakeJavaFile.add("      return FilenameUtils.getExtension(path.toString());");
      fakeJavaFile.add("   }");
      fakeJavaFile.add("");
      fakeJavaFile.add("   public static Path findFirstPathMatchingGlob(Path directory, final String glob)");
      fakeJavaFile.add("   {      ");
      fakeJavaFile.add("      final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(GLOB_SYNTAX_PREFIX + glob);");
      fakeJavaFile.add("      final Path[] pathHolder = {null};");
      fakeJavaFile.add("      ");
      fakeJavaFile.add("      walkRecursively(directory, new BasicPathVisitor()");
      fakeJavaFile.add("      {         ");
      fakeJavaFile.add("         @Override");
      fakeJavaFile.add("         public FileVisitResult visitPath(Path path, PathType pathType)");
      fakeJavaFile.add("         {");
      fakeJavaFile.add("            if (matcher.matches(path))");
      fakeJavaFile.add("            {");
      fakeJavaFile.add("               pathHolder[0] = path;");
      fakeJavaFile.add("               ");
      fakeJavaFile.add("               return FileVisitResult.TERMINATE;");
      fakeJavaFile.add("            }");
      fakeJavaFile.add("            ");
      fakeJavaFile.add("            return FileVisitResult.CONTINUE;");
      fakeJavaFile.add("         }");
      fakeJavaFile.add("      });");
      fakeJavaFile.add("");
      fakeJavaFile.add("      return pathHolder[0];");
      fakeJavaFile.add("   }");
      fakeJavaFile.add("");
      fakeJavaFile.add("   public static boolean directoryHasGlob(Path directory, final String glob)");
      fakeJavaFile.add("   {");
      fakeJavaFile.add("      return findFirstPathMatchingGlob(directory, glob) != null;");
      fakeJavaFile.add("   }");
      fakeJavaFile.add("");
      fakeJavaFile.add("   public static void walkRecursively(Path directory, final BasicPathVisitor basicFileVisitor)");
      fakeJavaFile.add("   {");
      fakeJavaFile.add("      try");
      fakeJavaFile.add("      {");
      fakeJavaFile.add("         Files.walkFileTree(directory, new SimpleFileVisitor<Path>()");
      fakeJavaFile.add("         {");
      fakeJavaFile.add("            @Override");
      fakeJavaFile.add("            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException");
      fakeJavaFile.add("            {");
      fakeJavaFile.add("               return basicFileVisitor.visitPath(dir, PathType.DIRECTORY);");
      fakeJavaFile.add("            }");
      fakeJavaFile.add("            ");
      fakeJavaFile.add("            @Override");
      fakeJavaFile.add("            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException");
      fakeJavaFile.add("            {");
      fakeJavaFile.add("               return basicFileVisitor.visitPath(file, PathType.FILE);");
      fakeJavaFile.add("            }");
      fakeJavaFile.add("         });");
      fakeJavaFile.add("      }");
      fakeJavaFile.add("      catch (IOException e)");
      fakeJavaFile.add("      {");
      fakeJavaFile.add("      }");
      fakeJavaFile.add("   }");
      fakeJavaFile.add("   ");
      fakeJavaFile.add("   public static void walkDepth(final Path directory, int maxDepth, final BasicPathVisitor basicFileVisitor)");
      fakeJavaFile.add("   {");
      fakeJavaFile.add("      try");
      fakeJavaFile.add("      {");
      fakeJavaFile.add("         Files.walkFileTree(directory, EnumSet.noneOf(FileVisitOption.class), maxDepth, new SimpleFileVisitor<Path>()");
      fakeJavaFile.add("         {");
      fakeJavaFile.add("            @Override");
      fakeJavaFile.add("            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException");
      fakeJavaFile.add("            {");
      fakeJavaFile.add("               if (dir.equals(directory))");
      fakeJavaFile.add("                  return FileVisitResult.CONTINUE;");
      fakeJavaFile.add("               ");
      fakeJavaFile.add("               return basicFileVisitor.visitPath(dir, PathType.DIRECTORY);");
      fakeJavaFile.add("            }");
      fakeJavaFile.add("            ");
      fakeJavaFile.add("            @Override");
      fakeJavaFile.add("            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException");
      fakeJavaFile.add("            {");
      fakeJavaFile.add("               if (Files.isDirectory(file))");
      fakeJavaFile.add("               {");
      fakeJavaFile.add("                  return basicFileVisitor.visitPath(file, PathType.DIRECTORY);");
      fakeJavaFile.add("               }");
      fakeJavaFile.add("               {");
      fakeJavaFile.add("                  return basicFileVisitor.visitPath(file, PathType.FILE);");
      fakeJavaFile.add("               }");
      fakeJavaFile.add("            }");
      fakeJavaFile.add("         });");
      fakeJavaFile.add("      }");
      fakeJavaFile.add("      catch (IOException e)");
      fakeJavaFile.add("      {");
      fakeJavaFile.add("      }");
      fakeJavaFile.add("   }");
      fakeJavaFile.add("");
      fakeJavaFile.add("   public static void walkFlat(final Path directory, final BasicPathVisitor basicFileVisitor)");
      fakeJavaFile.add("   {");
      fakeJavaFile.add("      walkDepth(directory, 1, basicFileVisitor);");
      fakeJavaFile.add("   }");
      fakeJavaFile.add("}");
      return fakeJavaFile;
   }

   public static void main(String[] args)
   {
      MutationTestFacilitator.facilitateMutationTestForClass(PathTools.class, PathToolsTest.class);
   }
}
