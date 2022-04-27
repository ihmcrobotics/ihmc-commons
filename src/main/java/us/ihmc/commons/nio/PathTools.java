package us.ihmc.commons.nio;

import org.apache.commons.io.FilenameUtils;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.nio.BasicPathVisitor.PathType;
import us.ihmc.log.LogTools;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * <p>A collection of tools to extend Java's NIO.2 API and
 * Apache Commons Lang. Tools here should fit one of
 * the following categories:</p>
 *
 * <ol>Provide a commonly needed method not provided by Apache Commons Lang or Java's NIO.2. API.</ol>
 * <ol>Provide a wrapper around a commonly used method that uses a {@link DefaultExceptionHandler}.</ol>
 * <ol>Provide a bridge between Java's NIO.2 API and Apache Commons Lang.</ol>
 */
public class PathTools
{
   private static final String GLOB_SYNTAX_PREFIX = "glob:";
   private static final String REGEX_SYNTAX_PREFIX = "regex:";

   /**
    * Get the base name of a file. A bridge from Java's NIO.2 to Apache Commons IO.
    *
    * @param path path
    * @return baseName the base name, minus the full path and extension, from a full filename
    */
   public static String getBaseName(Path path)
   {
      return FilenameUtils.getBaseName(path.toString());
   }

   /**
    * Get the extension of a file. A bridge from Java's NIO.2 to Apache Commons IO.
    *
    * @param path path
    * @return extension the extension of a file name
    */
   public static String getExtension(Path path)
   {
      return FilenameUtils.getExtension(path.toString());
   }

   public static Path systemTemporaryDirectory()
   {
      return Paths.get(System.getProperty("java.io.tmpdir"));
   }

   /**
    * Find a list of all Paths that match regex.
    *
    * @param directory directory to search
    * @param regex regular expression as defined by {@link java.util.regex.Pattern}
    * @return List of matching Paths.
    * @see {@link java.util.regex.Pattern}
    */
   public static List<Path> findAllPathsRecursivelyThatMatchRegex(Path directory, String regex)
   {
      final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(REGEX_SYNTAX_PREFIX + regex);
      final List<Path> matchingPaths = new ArrayList<>();

      walkRecursively(directory, (path, pathType) ->
      {
         if (matcher.matches(path))
         {
            matchingPaths.add(path);
         }

         return FileVisitResult.CONTINUE;
      });

      return matchingPaths;
   }

   /**
    * Find the first Path that matches the glob.
    *
    * @param directory directory to search
    * @param glob glob as defined by {@link PathMatcher}
    * @return List of matching Paths.
    * @see {@link PathMatcher}
    */
   public static Path findFirstPathMatchingGlob(Path directory, final String glob)
   {
      final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(GLOB_SYNTAX_PREFIX + glob);
      final Path[] pathHolder = {null};

      walkRecursively(directory, new BasicPathVisitor()
      {
         @Override
         public FileVisitResult visitPath(Path path, PathType pathType)
         {
            if (matcher.matches(path))
            {
               pathHolder[0] = path;

               return FileVisitResult.TERMINATE;
            }

            return FileVisitResult.CONTINUE;
         }
      });

      return pathHolder[0];
   }

   /**
    * Determines if there is a file or directory that matches <code>glob</code>.
    *
    * @param directory directory to search
    * @param glob glob as defined by {@link PathMatcher}
    * @return Has glob boolean.
    * @see {@link PathMatcher}
    */
   public static boolean directoryHasGlob(Path directory, final String glob)
   {
      return findFirstPathMatchingGlob(directory, glob) != null;
   }

   /**
    * Recursively walk through a directory. A simple case of Files.walkFileTree provided by Java's NIO.2.
    *
    * <p>WARNING: This method is best try only. All exceptions will be swallowed silently. For more specific behavior
    * you must use {@link Files#walkFileTree(Path, FileVisitor)} directly.</p>
    *
    * @param directory directory to walk
    * @param basicFileVisitor callback to take action on visits
    */
   public static void walkRecursively(Path directory, final PathVisitor basicFileVisitor)
   {
      try
      {
         Files.walkFileTree(directory, new SimpleFileVisitor<Path>()
         {
            @Override
            public FileVisitResult preVisitDirectory(Path preVisitDirectory, BasicFileAttributes attributes) throws IOException
            {
               return basicFileVisitor.visitPath(preVisitDirectory, PathType.DIRECTORY);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException
            {
               return basicFileVisitor.visitPath(file, PathType.FILE);
            }
         });
      }
      catch (IOException e)
      {
      }
   }

   /**
    * <p>Walk through a directory to a max depth. A simple case of Files.walkFileTree provided by Java's NIO.2.</p>
    *
    * <p>WARNING: This method is best try only. All exceptions will be swallowed silently. For more specific behavior
    * you must use {@link Files#walkFileTree(Path, FileVisitor)} directly.</p>
    *
    * @param directory directory to walk
    * @param basicFileVisitor callback to take action on visits
    */
   public static void walkDepth(final Path directory, int maxDepth, final PathVisitor basicFileVisitor)
   {
      try
      {
         Files.walkFileTree(directory, EnumSet.noneOf(FileVisitOption.class), maxDepth, new SimpleFileVisitor<Path>()
         {
            @Override
            public FileVisitResult preVisitDirectory(Path preVisitDirectory, BasicFileAttributes attributes) throws IOException
            {
               if (preVisitDirectory.equals(directory))
               {
                  return FileVisitResult.CONTINUE;
               }

               return basicFileVisitor.visitPath(preVisitDirectory, PathType.DIRECTORY);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException
            {
               if (Files.isDirectory(file))
               {
                  return basicFileVisitor.visitPath(file, PathType.DIRECTORY);
               }
               else
               {
                  return basicFileVisitor.visitPath(file, PathType.FILE);
               }
            }
         });
      }
      catch (IOException e)
      {
      }
   }

   /**
    * Walk through a directory's immediate contents without diving deeper.
    * A simple case of Files.walkFileTree provided by Java's NIO.2.
    *
    * <p>WARNING: This method is best try only. All exceptions will be swallowed silently. For more specific behavior
    * you must use {@link Files#walkFileTree(Path, FileVisitor)} directly.</p>
    *
    * @param directory directory to walk
    * @param basicFileVisitor callback to take action on visits
    */
   public static void walkFlat(final Path directory, final PathVisitor basicFileVisitor)
   {
      walkDepth(directory, 1, basicFileVisitor);
   }

   /**
    * Searches for a directory without leaving the current tree branch.
    * In other words, search for the directory as a child or as part of the starting path.
    *
    * @param startingDirectory path string to the directory to start
    * @param directoryNameToFind name of the directory to find
    * @param fallback If directoryNameToFind not found, return this
    * @return absolute normalized path to directoryToFind or fallback
    */
   public static Path findDirectoryInline(String startingDirectory, String directoryNameToFind, Path fallback)
   {
      Path startingDirectoryPath = Paths.get(startingDirectory).toAbsolutePath().normalize();
      Path currentDirectoryPath = startingDirectoryPath;

      if (startingDirectory.equals(directoryNameToFind))
      {
         return currentDirectoryPath;
      }

      if (Files.exists(currentDirectoryPath.resolve(directoryNameToFind)))
      {
         return currentDirectoryPath.resolve(directoryNameToFind);
      }

      Path root = Paths.get("/").toAbsolutePath().normalize();

      currentDirectoryPath = currentDirectoryPath.resolve("..").normalize();
      do
      {
         Path fileName = currentDirectoryPath.getFileName();
         if (fileName != null && fileName.toString().equals(directoryNameToFind))
         {
            return currentDirectoryPath;
         }

         currentDirectoryPath = currentDirectoryPath.resolve("..").normalize();
      }
      while (!currentDirectoryPath.equals(root));

      if (fallback == null)
      {
         LogTools.warn("{} directory could not be found. Starting directory: {}", directoryNameToFind, startingDirectoryPath);
         return null;
      }
      else
      {
         return fallback.toAbsolutePath().normalize();
      }
   }

   /**
    * Searches for a directory without leaving the current tree branch.
    * In other words, search for the directory as a child or as part of the current working directory.
    *
    * @param directoryNameToFind name of the directory to find
    * @return absolute normalized path to directoryToFind or null
    */
   public static Path findDirectoryInline(String directoryNameToFind)
   {
      return findDirectoryInline(".", directoryNameToFind, null);
   }

   /**
    * Searches for a directory without leaving the current tree branch.
    * In other words, search for the directory as a child or as part of the starting path.
    *
    * @param startingDirectory path string to the directory to start
    * @param directoryNameToFind name of the directory to find
    * @param fallback If directoryNameToFind not found, return this
    * @param subsequentPathToResolve attempt to resolve this path at the end
    * @return absolute normalized path to directoryToFind or fallback, plus the subsequent path to resolve
    */
   public static Path findPathInline(String startingDirectory, String directoryNameToFind, String subsequentPathToResolve, Path fallback)
   {
      Path directoryInline = findDirectoryInline(startingDirectory, directoryNameToFind, fallback);

      if (directoryInline.equals(fallback.toAbsolutePath().normalize())) // subsequent not applied in case of fallback
      {
         return directoryInline;
      }

      Path subsequentPath = directoryInline.resolve(subsequentPathToResolve).normalize();

      if (!Files.exists(subsequentPath))
         throw new RuntimeException("Subsequent path does not exist");

      return subsequentPath;
   }

   /**
    * Searches for a directory without leaving the current tree branch.
    * In other words, search for the directory as a child or as part of the starting path.
    *
    * @param startingDirectory path string to the directory to start
    * @param directoryNameToFind name of the directory to find
    * @param subsequentPathToResolve attempt to resolve this path at the end
    * @return absolute normalized path to directoryToFind or fallback, plus the subsequent path to resolve
    */
   public static Path findPathInline(String startingDirectory, String directoryNameToFind, String subsequentPathToResolve)
   {
      Path directoryInline = findDirectoryInline(startingDirectory, directoryNameToFind, null);

      if (directoryInline == null) // subsequent not applied in case of fallback
         throw new RuntimeException("Could not find directory " + directoryNameToFind + " in " + Paths.get(startingDirectory).toAbsolutePath().normalize());

      Path subsequentPath = directoryInline.resolve(subsequentPathToResolve).normalize();

      if (!Files.exists(subsequentPath))
         throw new RuntimeException("Subsequent path does not exist");

      return subsequentPath;
   }
}
