package us.ihmc.commons;

import java.util.HashMap;
import java.util.logging.*;

/**
 * Unfortunately, this class can't configure the log level for logback based libraries,
 * because SLF4J doesn't provide the setLevel API.
 */
public class LogTools
{
   /** Keep a list of loggers, so we don't recreate a bunch of formatters */
   private static final HashMap<String, Logger> loggers = new HashMap<>();

   /** For keeping track of the class that actually calls the LogTools method. */
   private static final int STACK_TRACE_INDEX = 1;

   public static final void setLevel(Level level)
   {
      setLevel(className(new Throwable()), level);
   }

   public static final void setLevel(Class<?> clazz, Level level)
   {
      setLevel(clazz.getName(), level);
   }

   public static final void setLevel(String classMatcher, Level level)
   {
      if (loggers.containsKey(classMatcher))
      {
         setLevelInternal(level, classMatcher);
      }
      else
      {
         for (String loggerName : loggers.keySet())
         {
            if (loggerName.startsWith(classMatcher))
            {
               setLevelInternal(level, loggerName);
            }
         }
      }
   }

   private static void setLevelInternal(Level level, String loggerName)
   {
      Logger logger = loggers.get(loggerName);
      logger.setLevel(level);

      logger.getHandlers()[0].setLevel(level);

      if (level.intValue() < Level.WARNING.intValue())
      {
         logger.getHandlers()[1].setLevel(Level.WARNING);
      }
      else
      {
         logger.getHandlers()[1].setLevel(level);
      }
   }

   public static final Logger getLogger(Class<?> clazz)
   {
      return getLogger(clazz.getName());
   }

   public static final Logger getLogger(String className)
   {
      checkLoggerCreated(className);

      return loggers.get(className);
   }

   private static void checkLoggerCreated(String className)
   {
      if (!loggers.containsKey(className))
      {
         Logger logger = Logger.getLogger(className);

         // Track the loggers that use this class
         loggers.put(className, logger);

         // This line is questionable. It's to disable the default handler
         // Another solution could be to use LogManager.getLogManager().reset();
         logger.setUseParentHandlers(false);

         // Set up INFO, CONFIG, FINE, FINER, FINEST to use System.out
         StreamHandler infoAndDownHandler = new StreamHandler(System.out, createFormatter());
         infoAndDownHandler.setFilter(record -> record.getLevel().intValue() <= Level.INFO.intValue());
         logger.addHandler(infoAndDownHandler);

         // Set up SEVERE, WARNING to use System.err
         StreamHandler warningAndUpHandler = new StreamHandler(System.err, createFormatter());
         warningAndUpHandler.setLevel(Level.WARNING);
         logger.addHandler(warningAndUpHandler);
      }
   }

   private static Formatter createFormatter()
   {
      return new Formatter()
      {
         @Override
         public synchronized String format(LogRecord record)
         {
            String message = formatMessage(record);
            return "[" + record.getLevel().getLocalizedName() + "] " + message + "\n";
         }
      };
   }

   public static void severe(String message)
   {
      Throwable throwable = new Throwable();
      getLogger(className(throwable)).severe(log(null, message, throwable));
   }

   public static void warning(String message)
   {
      Throwable throwable = new Throwable();
      getLogger(className(throwable)).warning(log(null, message, throwable));
   }

   public static void info(String message)
   {
      Throwable throwable = new Throwable();
      getLogger(className(throwable)).info(log(null, message, throwable));
   }

   public static void config(String message)
   {
      Throwable throwable = new Throwable();
      getLogger(className(throwable)).config(log(null, message, throwable));
   }

   public static void fine(String message)
   {
      Throwable throwable = new Throwable();
      getLogger(className(throwable)).fine(log(null, message, throwable));
   }

   public static void finer(String message)
   {
      Throwable throwable = new Throwable();
      getLogger(className(throwable)).finer(log(null, message, throwable));
   }

   public static void finest(String message)
   {
      Throwable throwable = new Throwable();
      getLogger(className(throwable)).finest(log(null, message, throwable));
   }

   public static void severe(Logger logger, String message)
   {
      logger.severe(log(null, message, new Throwable()));
   }

   public static void warning(Logger logger, String message)
   {
      logger.warning(log(null, message, new Throwable()));
   }

   public static void info(Logger logger, String message)
   {
      logger.info(log(null, message, new Throwable()));
   }

   public static void config(Logger logger, String message)
   {
      logger.config(log(null, message, new Throwable()));
   }

   public static void fine(Logger logger, String message)
   {
      logger.fine(log(null, message, new Throwable()));
   }

   public static void finer(Logger logger, String message)
   {
      logger.finer(log(null, message, new Throwable()));
   }

   public static void finest(Logger logger, String message)
   {
      logger.finest(log(null, message, new Throwable()));
   }

   private static String className(Throwable throwable)
   {
      return throwable.getStackTrace()[STACK_TRACE_INDEX].getClassName().split("\\.java")[0];
   }

   private static String log(Object containingObjectOrClass, String message, Throwable throwable)
   {
      int lineNumber = -1;
      String className;
      if (containingObjectOrClass == null)
      {
         String[] classNameSplit = throwable.getStackTrace()[STACK_TRACE_INDEX].getClassName().split("\\.");
         className = classNameSplit[classNameSplit.length - 1].split("\\$")[0];
         lineNumber = throwable.getStackTrace()[STACK_TRACE_INDEX].getLineNumber();
      }
      else
      {
         className = containingObjectOrClass.getClass().getSimpleName();

         for (StackTraceElement stackTraceElement : throwable.getStackTrace())
         {
            if (stackTraceElement.getClassName().endsWith(className))
            {
               lineNumber = stackTraceElement.getLineNumber();
               break;
            }
         }

         if (lineNumber == -1)
            lineNumber = throwable.getStackTrace()[STACK_TRACE_INDEX].getLineNumber();

         if (containingObjectOrClass instanceof Class<?>)
            className = ((Class<?>) containingObjectOrClass).getSimpleName();
      }

      String clickableLocation = "(" + className + ".java:" + lineNumber + ")";

      return clickableLocation + ": " + message;
   }
}
