package us.ihmc.commons.nio;

import org.apache.commons.io.FileUtils;
import us.ihmc.commons.Conversions;
import us.ihmc.commons.exception.ExceptionHandler;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>A collection of tools to extend Java's NIO.2 API and
 * Apache Commons Lang. Tools here should fit one of
 * the following categories:</p>
 *
 * <ol>Provide a commonly needed method not provided by Apache Commons Lang or Java's NIO.2. API.</ol>
 * <ol>Provide a wrapper around a commonly used method that uses a {@link ExceptionHandler}.</ol>
 * <ol>Provide a bridge between Java's NIO.2 API and Apache Commons Lang.</ol>
 */
public class FileTools
{
   /** A carriage return */
   private static final byte CARRIAGE_RETURN = '\r';

   /** A newline */
   private static final byte NEWLINE = '\n';

   /**
    * Delete a file or directory quietly. A bridge from Java's NIO.2 to Apache Commons IO.
    *
    * @param path file or directory to be deleted
    * @see {@link FileUtils#deleteQuietly(File)}
    */
   public static void deleteQuietly(Path path)
   {
      FileUtils.deleteQuietly(path.toFile());
   }

   /**
    * Reads all lines from a file. Uses Java's NIO.2 API.
    *
    * <p>WARNING: For use only when there is no meaningful way to handle failure.</p>
    *
    * @param path file to read lines from
    * @param exceptionHandler functional exception handler
    * @return list of strings
    * @see {@link java.nio.file.Files#readAllLines(Path)}
    */
   @SuppressWarnings("unchecked")
   public static List<String> readAllLines(Path path, ExceptionHandler exceptionHandler)
   {
      try
      {
         return Files.readAllLines(path);
      }
      catch (IOException ioException)
      {
         exceptionHandler.handleException(ioException);
         return null;
      }
   }

   /**
    * Reads all lines from a file. Uses Java's NIO.2 API.
    *
    * <p>WARNING: For use only when there is no meaningful way to handle failure.</p>
    *
    * @param path file to read lines from
    * @param charset character set to use
    * @param exceptionHandler functional exception handler
    * @return list of strings
    * @see {@link java.nio.file.Files#readAllLines(Path, Charset)}
    */
   @SuppressWarnings("unchecked")
   public static List<String> readAllLines(Path path, Charset charset, ExceptionHandler exceptionHandler)
   {
      try
      {
         return Files.readAllLines(path, charset);
      }
      catch (IOException ioException)
      {
         exceptionHandler.handleException(ioException);
         return null;
      }
   }

   /**
    * Reads all the bytes from a file. Uses Java's NIO.2 API.
    *
    * <p>WARNING: For use only when there is no meaningful way to handle failure.</p>
    *
    * @param path file to read lines from
    * @param exceptionHandler functional exception handler
    * @return File as a byte array.
    * @see {@link java.nio.file.Files#readAllBytes(Path)}
    */
   public static byte[] readAllBytes(Path path, ExceptionHandler exceptionHandler)
   {
      try
      {
         return Files.readAllBytes(path);
      }
      catch (IOException ioException)
      {
         exceptionHandler.handleException(ioException);
         return null;
      }
   }

   /**
    * Writes bytes to a file. Uses Java's NIO.2 API.
    *
    * <p>WARNING: For use only when there is no meaningful way to handle failure.</p>
    *
    * @param path file to write to
    * @param bytes bytes to write
    * @param writeOption append or truncate
    * @param exceptionHandler functional exception handler
    * @see {@link java.nio.file.Files#write(Path, byte[], OpenOption...)}
    */
   public static void write(Path path, byte[] bytes, WriteOption writeOption, ExceptionHandler exceptionHandler)
   {
      try
      {
         Files.write(path, bytes, writeOption.getOptions());
      }
      catch (IOException ioException)
      {
         exceptionHandler.handleException(ioException);
      }
   }

   /**
    * Write lines of text to a file. Uses Java's NIO.2 API.
    *
    * <p>WARNING: For use only when there is no meaningful way to handle failure.</p>
    *
    * @param path file to write to
    * @param lines lines to write
    * @param writeOption append or truncate
    * @param exceptionHandler functional exception handler
    * @see {@link java.nio.file.Files#write(Path, Iterable, OpenOption...)}
    */
   public static void write(Path path, Iterable<? extends CharSequence> lines, WriteOption writeOption, ExceptionHandler exceptionHandler)
   {
      try
      {
         Files.write(path, lines, writeOption.getOptions());
      }
      catch (IOException ioException)
      {
         exceptionHandler.handleException(ioException);
      }
   }

   /**
    * Write lines of text to a file. Uses Java's NIO.2 API.
    *
    * <p>WARNING: For use only when there is no meaningful way to handle failure.</p>
    *
    * @param path file to write to
    * @param lines lines to write
    * @param charset charset to use
    * @param writeOption append or truncate
    * @param exceptionHandler functional exception handler
    * @see {@link java.nio.file.Files#write(Path, Iterable, Charset, OpenOption...)}
    */
   public static void write(Path path, Iterable<? extends CharSequence> lines, Charset charset, WriteOption writeOption,
                            ExceptionHandler exceptionHandler)
   {
      try
      {
         Files.write(path, lines, charset, writeOption.getOptions());
      }
      catch (IOException ioException)
      {
         exceptionHandler.handleException(ioException);
      }
   }

   /**
    * Write a list of Strings to lines in a file. Uses Java's NIO.2 API.
    *
    * <p>WARNING: For use only when there is no meaningful way to handle failure.</p>
    *
    * @param lines lines to be written
    * @param path file
    * @param writeOption append or truncate
    * @param exceptionHandler functional exception handler
    */
   public static void writeAllLines(List<String> lines, Path path, WriteOption writeOption, ExceptionHandler exceptionHandler)
   {
      PrintWriter printer = newPrintWriter(path, writeOption, exceptionHandler);
      lines.forEach(line -> printer.println(line));
      printer.close();
   }

   /**
    * Creates a new PrintWriter. Uses Java's NIO.2 API.
    *
    * <p>WARNING: For use only when there is no meaningful way to handle failure.</p>
    *
    * @param path file to open
    * @param writeOption truncate or append
    * @param exceptionHandler functional exception handler
    * @return new print writer
    */
   public static PrintWriter newPrintWriter(Path path, WriteOption writeOption, ExceptionHandler exceptionHandler)
   {
      try
      {
         return newPrintWriter(path, writeOption);
      }
      catch (IOException ioException)
      {
         exceptionHandler.handleException(ioException);
         return null;
      }
   }

   /**
    * Creates a new PrintWriter. Uses Java's NIO.2 API.
    *
    * @param path file to open
    * @param writeOption append or truncate
    * @return new print writer
    * @throws IOException
    * @see {@link Files#newBufferedWriter(Path, OpenOption...)}
    */
   public static PrintWriter newPrintWriter(Path path, WriteOption writeOption) throws IOException
   {
      return new PrintWriter(Files.newBufferedWriter(path, writeOption.getOptions()));
   }

   /**
    * Read bytes into a list of strings using {@link BufferedReader#readLine()}.
    *
    * <p>WARNING: For use only when there is no meaningful way to handle failure.</p>
    *
    * @param bytes bytes to read
    * @param exceptionHandler functional exception handler
    * @return list of strings
    */
   public static List<String> readLinesFromBytes(byte[] bytes, ExceptionHandler exceptionHandler)
   {
      List<String> lines = new ArrayList<>();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8.newDecoder())))
      {
         while (true)
         {
            String line = reader.readLine();
            if (line != null)
            {
               lines.add(line);
            }
            else
            {
               break;
            }
         }
      }
      catch (IOException ioException)
      {
         exceptionHandler.handleException(ioException);
      }
      return lines;
   }

   /**
    * Replace a line in a file by index with a replacement line. For efficiency, it is required
    * to pass in the file as an array of bytes and also as a list of strings by line.
    *
    * @param lineIndex line number starting at 0 of line to replace
    * @param newLine replacement line
    * @param fileBytes file as an array of bytes
    * @param fileLines file as a list of lines
    * @return updated file with replacement line as an array of bytes
    */
   public static byte[] replaceLineInFile(int lineIndex, String newLine, byte[] fileBytes, List<String> fileLines)
   {
      byte[] newBytes = new byte[fileBytes.length - fileLines.get(lineIndex).length() + newLine.length()];

      int newBytesIndex = 0;
      int fileBytesIndex = 0;
      for (int fileLineIndex = 0; fileLineIndex < fileLines.size(); fileLineIndex++)
      {
         if (fileLineIndex == lineIndex)
         {
            for (byte b : newLine.getBytes())
            {
               newBytes[newBytesIndex++] = b;
            }

            fileBytesIndex += fileLines.get(fileLineIndex).length();
            fileLines.set(fileLineIndex, newLine);
         }
         else
         {
            for (byte b : fileLines.get(fileLineIndex).getBytes())
            {
               newBytes[newBytesIndex++] = b;
               ++fileBytesIndex;
            }
         }

         if (fileBytes.length > fileBytesIndex + 1 && fileBytes[fileBytesIndex] == CARRIAGE_RETURN && fileBytes[fileBytesIndex + 1] == NEWLINE)
         {
            newBytes[newBytesIndex++] = fileBytes[fileBytesIndex++];
            newBytes[newBytesIndex++] = fileBytes[fileBytesIndex++];
         }
         else if (fileBytesIndex < fileBytes.length && (fileBytes[fileBytesIndex] == CARRIAGE_RETURN || fileBytes[fileBytesIndex] == NEWLINE))
         {
            newBytes[newBytesIndex++] = fileBytes[fileBytesIndex++];
         }
      }

      return newBytes;
   }

   /**
    * <p>Ensure directory exists. Performs the following:</p>
    *
    * <li>Recursively perform this on parents.</li>
    * <li>If <code>path</code> is a file, delete and make new directory.</li>
    * <li>If <code>path</code> does not exist, create directory.</li>
    *
    * @param path path of directory to ensure existence of
    * @throws IOException
    */
   public static void ensureDirectoryExists(Path path) throws IOException
   {
      if (path.getParent() != null && !Files.exists(path.getParent()))
      {
         ensureDirectoryExists(path.getParent());
      }

      if (Files.exists(path) && !Files.isDirectory(path))
      {
         FileTools.deleteQuietly(path);
         Files.createDirectory(path);
      }

      if (!Files.exists(path))
      {
         Files.createDirectory(path);
      }
   }

   /**
    * <p>Ensure directory exists. Performs the following:</p>
    *
    * <li>Recursively perform this on parents.</li>
    * <li>If <code>path</code> is a file, delete and make new directory.</li>
    * <li>If <code>path</code> does not exist, create directory.</li>
    *
    * <p>WARNING: For use only when there is no meaningful way to handle failure.</p>
    *
    * @param path path of directory to ensure existence of
    * @param exceptionHandler functional exception handler
    */
   public static void ensureDirectoryExists(Path path, ExceptionHandler exceptionHandler)
   {
      try
      {
         ensureDirectoryExists(path);
      }
      catch (IOException ioException)
      {
         exceptionHandler.handleException(ioException);
      }
   }

   /**
    * <p>Ensure file exists. Performs the following:</p>
    *
    * <li>Recursively perform this on parents.</li>
    * <li>If <code>path</code> is a directory, delete and create new file.</li>
    * <li>If <code>path</code> does not exist, create file.</li>
    *
    * @param path path of file to ensure existence of
    * @throws IOException
    */
   public static void ensureFileExists(Path path) throws IOException
   {
      if (path.getParent() != null && !Files.exists(path.getParent()))
      {
         ensureDirectoryExists(path.getParent());
      }

      if (Files.exists(path) && Files.isDirectory(path))
      {
         FileTools.deleteQuietly(path);
         Files.createFile(path);
      }

      if (!Files.exists(path))
      {
         Files.createFile(path);
      }
   }

   /**
    * <p>Ensure file exists. Performs the following:</p>
    *
    * <li>Recursively perform this on parents.</li>
    * <li>If <code>path</code> is a directory, delete and create new file.</li>
    * <li>If <code>path</code> does not exist, create file.</li>
    *
    * <p>WARNING: For use only when there is no meaningful way to handle failure.</p>
    *
    * @param path path of file to ensure existence of
    */
   public static void ensureFileExists(Path path, ExceptionHandler exceptionHandler)
   {
      try
      {
         ensureFileExists(path);
      }
      catch (IOException ioException)
      {
         exceptionHandler.handleException(ioException);
      }
   }

   /**
    * Concatenate N files together into one file.
    *
    * @param filesToConcatenate files to concatenate
    * @param concatenatedFile concatenated file
    * @throws IOException
    */
   public static void concatenateFiles(List<Path> filesToConcatenate, Path concatenatedFile) throws IOException
   {
      DataOutputStream concatenatedFileOutputStream = newFileDataOutputStream(concatenatedFile);

      for (Path fileToConcatenate : filesToConcatenate)
      {
         DataInputStream fileToConcatenateInputStream = newFileDataInputStream(fileToConcatenate);

         while (fileToConcatenateInputStream.available() > 0)
         {
            concatenatedFileOutputStream.write(fileToConcatenateInputStream.read());
         }
      }

      concatenatedFileOutputStream.flush();
   }

   /**
    * Concatenate N files together into one file.
    *
    * <p>WARNING: For use only when there is no meaningful way to handle failure.</p>
    *
    * @param filesToConcatenate files to concatenate
    * @param concatenatedFile concatenated file
    * @param exceptionHandler functional exception handler
    * @throws IOException
    */
   public static void concatenateFiles(List<Path> filesToConcatenate, Path concatenatedFile, ExceptionHandler exceptionHandler)
   {
      try
      {
         concatenateFiles(filesToConcatenate, concatenatedFile);
      }
      catch (IOException ioException)
      {
         exceptionHandler.handleException(ioException);
      }
   }

   /**
    * Creates a new data output stream to a file for writing.
    *
    * @param file file to create stream from
    * @return dataOutputStream file data output stream
    * @throws FileNotFoundException
    * @see {@link DataOutputStream}, {@link BufferedOutputStream}, {@link FileOutputStream}.
    */
   public static DataOutputStream newFileDataOutputStream(Path file) throws FileNotFoundException
   {
      return newFileDataOutputStream(file, Conversions.kibibytesToBytes(8));
   }

   /**
    * Creates a new data output stream to a file for writing.
    *
    * @param file file to create stream from
    * @param bufferSizeInBytes buffer size in bytes
    * @return dataOutputStream file data output stream
    * @throws FileNotFoundException
    * @see {@link DataOutputStream}, {@link BufferedOutputStream}, {@link FileOutputStream}.
    */
   public static DataOutputStream newFileDataOutputStream(Path file, int bufferSizeInBytes) throws FileNotFoundException
   {
      return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file.toFile()), bufferSizeInBytes));
   }

   /**
    * Creates a new data output stream to a file for writing.
    *
    * <p>WARNING: For use only when there is no meaningful way to handle failure.</p>
    *
    * @param file file to create stream from
    * @param exceptionHandler functional exception handler
    * @return dataOutputStream file data output stream
    * @throws FileNotFoundException
    * @see {@link DataOutputStream}, {@link BufferedOutputStream}, {@link FileOutputStream}.
    */
   public static DataOutputStream newFileDataOutputStream(Path file, ExceptionHandler exceptionHandler)
   {
      return newFileDataOutputStream(file, Conversions.kibibytesToBytes(8), exceptionHandler);
   }

   /**
    * Creates a new data output stream to a file for writing.
    *
    * <p>WARNING: For use only when there is no meaningful way to handle failure.</p>
    *
    * @param file file to create stream from
    * @param bufferSizeInBytes buffer size in bytes
    * @param exceptionHandler functional exception handler
    * @return dataOutputStream file data output stream
    * @throws FileNotFoundException
    * @see {@link DataOutputStream}, {@link BufferedOutputStream}, {@link FileOutputStream}.
    */
   public static DataOutputStream newFileDataOutputStream(Path file, int bufferSizeInBytes, ExceptionHandler exceptionHandler)
   {
      try
      {
         return newFileDataOutputStream(file, bufferSizeInBytes);
      }
      catch (FileNotFoundException fileNotFoundException)
      {
         exceptionHandler.handleException(fileNotFoundException);
         return null;
      }
   }

   /**
    * Creates a new data input stream to read from a file.
    *
    * @param file file to create stream from
    * @return dataInputStream file data input stream
    * @throws FileNotFoundException
    * @see {@link DataInputStream}, {@link BufferedInputStream}, {@link FileInputStream}.
    */
   public static DataInputStream newFileDataInputStream(Path file) throws FileNotFoundException
   {
      return newFileDataInputStream(file, Conversions.kibibytesToBytes(8));
   }

   /**
    * Creates a new data input stream to read from a file.
    *
    * @param file file to create stream from
    * @param bufferSizeInBytes buffer size in bytes
    * @return dataInputStream file data input stream
    * @throws FileNotFoundException
    * @see {@link DataInputStream}, {@link BufferedInputStream}, {@link FileInputStream}.
    */
   public static DataInputStream newFileDataInputStream(Path file, int bufferSizeInBytes) throws FileNotFoundException
   {
      return new DataInputStream(new BufferedInputStream(new FileInputStream(file.toFile()), bufferSizeInBytes));
   }

   /**
    * Creates a new data input stream to read from a file.
    *
    * <p>WARNING: For use only when there is no meaningful way to handle failure.</p>
    *
    * @param file file to create stream from
    * @param exceptionHandler functional exception handler
    * @return dataInputStream file data input stream
    * @throws FileNotFoundException
    * @see {@link DataInputStream}, {@link BufferedInputStream}, {@link FileInputStream}.
    */
   public static DataInputStream newFileDataInputStream(Path file, ExceptionHandler exceptionHandler)
   {
      return newFileDataInputStream(file, Conversions.kibibytesToBytes(8), exceptionHandler);
   }

   /**
    * Creates a new data input stream to read from a file.
    *
    * <p>WARNING: For use only when there is no meaningful way to handle failure.</p>
    *
    * @param file file to create stream from
    * @param bufferSizeInBytes buffer size in bytes
    * @param exceptionHandler functional exception handler
    * @return dataInputStream file data input stream
    * @throws FileNotFoundException
    * @see {@link DataInputStream}, {@link BufferedInputStream}, {@link FileInputStream}.
    */
   public static DataInputStream newFileDataInputStream(Path file, int bufferSizeInBytes, ExceptionHandler exceptionHandler)
   {
      try
      {
         return newFileDataInputStream(file, bufferSizeInBytes);
      }
      catch (FileNotFoundException fileNotFoundException)
      {
         exceptionHandler.handleException(fileNotFoundException);
         return null;
      }
   }
}
