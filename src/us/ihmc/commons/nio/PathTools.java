package us.ihmc.commons.nio;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

import org.apache.commons.io.FilenameUtils;

import us.ihmc.commons.nio.BasicPathVisitor.PathType;

public class PathTools
{
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

   /**
   * Recursively walk through a directory. A simple case of Files.walkFileTree provided by Java's NIO.2.
   * 
   * @param directory directory to walk
   * @param basicFileVisitor callback to take action on visits
   */
   public static void walkRecursively(Path directory, final BasicPathVisitor basicFileVisitor)
   {
      try
      {
         Files.walkFileTree(directory, new SimpleFileVisitor<Path>()
         {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
            {
               return basicFileVisitor.visitPath(dir, PathType.DIRECTORY);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
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
    * Walk through a directory to a max depth. A simple case of Files.walkFileTree provided by Java's NIO.2.
    * 
    * @param directory directory to walk
    * @param basicFileVisitor callback to take action on visits
    */
   public static void walkDepth(final Path directory, int maxDepth, final BasicPathVisitor basicFileVisitor)
   {
      try
      {
         Files.walkFileTree(directory, EnumSet.noneOf(FileVisitOption.class), maxDepth, new SimpleFileVisitor<Path>()
         {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
            {
               if (dir.equals(directory))
                  return FileVisitResult.CONTINUE;

               return basicFileVisitor.visitPath(dir, PathType.DIRECTORY);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
               if (Files.isDirectory(file))
               {
                  return basicFileVisitor.visitPath(file, PathType.DIRECTORY);
               }
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
    * @param directory directory to walk
    * @param basicFileVisitor callback to take action on visits
    */
   public static void walkFlat(final Path directory, final BasicPathVisitor basicFileVisitor)
   {
      walkDepth(directory, 1, basicFileVisitor);
   }
}
