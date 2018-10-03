package us.ihmc.commons.allocations;

import com.google.monitoring.runtime.instrumentation.AllocationRecorder;
import us.ihmc.commons.RunnableThatThrows;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.exception.ExceptionHandler;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A tool for finding and testing for allocations. Features include and exclude filters for scoping in on
 * certain classes and methods, but excluding known or "okay" allocations, or finding literally all allocations made
 * in the JVM between {@link AllocationProfiler#startRecordingAllocations()} and {@link AllocationProfiler#stopRecordingAllocations()}.
 */
public class AllocationProfiler
{
   /** State boolean for recording. True between {@link AllocationProfiler#startRecordingAllocations()}
    * and {@link AllocationProfiler#stopRecordingAllocations()} */
   private boolean recording = false;

   /** The allocation queue that fills up as allocations are recorded. */
   private final Queue<AllocationRecord> allocations = new ConcurrentLinkedQueue<>();

   /** Special flag to include everything. */
   private boolean includeAllAllocations;

   /** Exclude allocations that happen inside these methods. */
   private final Set<String> excludeAllocationsInsideTheseMethods = new HashSet<>();

   /** Include allocations that happen inside these methods. */
   private final Set<String> includeAllocationsInsideTheseMethods = new HashSet<>();

   /** Exclude allocations that happen inside these classes. */
   private final Set<String> excludeAllocationsInsideTheseClasses = new HashSet<>();

   /** Include allocations that happen inside these classes. */
   private final Set<String> includeAllocationsInsideTheseClasses = new HashSet<>();

   /** Exclude allocations of the classes (i.e. new ClassName()). */
   private final Set<String> excludeAllocationsOfTheseClasses = new HashSet<>();

   /** Include allocations of the classes (i.e. new ClassName()). */
   private final Set<String> includeAllocationsOfTheseClasses = new HashSet<>();

   /** Include allocations whose stack trace leading up to the allocation contains one of these keywords. */
   private final Set<String> includeAllocationsWhoseTracesContainTheseKeywords = new HashSet<>();

   /** Exclude allocations whose stack trace leading up to the allocation contains one of these keywords. */
   private final Set<String> excludeAllocationsWhoseTracesContainTheseKeywords = new HashSet<>();

   /**
    * <p>Create a new allocation profiler and call {@link AllocationProfiler#reset()}.</p>
    *
    * <p>By default, records all allocations EXCEPT:</p>
    * <li>Does not record any allocations inside constructors</li>
    * <li>Does not record any allocations from and resulting from static member initialization</li>
    * <li>Does not record anything as a result of the ClassLoader loading classes</li>
    * <li>Does not record any allocations made by this profiler, Gradle, or JUnit</li>
    *
    */
   public AllocationProfiler()
   {
      reset();
   }

   /**
    * Start recording allocations.
    */
   public void startRecordingAllocations()
   {
      checkInstrumentation();

      recording = true;
      AllocationRecorder.addSampler(this::sampleAllocation);
   }

   /**
    * Stop recording allocations.
    */
   public void stopRecordingAllocations()
   {
      recording = false;
      AllocationRecorder.removeSampler(this::sampleAllocation); // remove sampler to speed up execution of unmonitored code
   }

   /**
    * Poll the recorded allocations excluding duplicate entries. This removes them from the queue.
    *
    * @return allocations
    */
   public List<AllocationRecord> pollAllocations()
   {
      return removeDuplicateRecords(pollAllocationsIncludingDuplicates());
   }

   /**
    * Helper method to remove duplicate stack traces from a list of allocations.
    *
    * @param allocations list to prune of duplicate stack traces.
    * @return list of allocations with unique stack traces.
    */
   public static List<AllocationRecord> removeDuplicateRecords(List<AllocationRecord> allocations)
   {
      Map<String, AllocationRecord> map = new HashMap<>();
      allocations.forEach(t -> map.put(t.toString(), t));
      return new ArrayList<>(map.values());
   }

   /**
    * Poll all allocations including duplicates.
    *
    * @return allocations
    */
   public List<AllocationRecord> pollAllocationsIncludingDuplicates()
   {
      List<AllocationRecord> allocations = new ArrayList<>();
      AllocationRecord allocation;
      while ((allocation = this.allocations.poll()) != null)
      {
         allocations.add(allocation);
      }
      return allocations;
   }

   /**
    * Will run the provided runnable and return a list of places where allocations occurred.
    * If the returned list is empty no allocations were detected.
    *
    * @param runnable contains the code to be profiled.
    * @return a list of places where objects were allocated.
    */
   public List<AllocationRecord> recordAllocations(Runnable runnable)
   {
      return recordAllocations(() -> runnable.run(), DefaultExceptionHandler.PROCEED_SILENTLY);
   }

   /**
    * Will run the provided runnable and return a list of places where allocations occurred.
    * If the returned list is empty no allocations where detected.
    *
    * Allows for exceptions to be handled easily.
    *
    * @param runnable contains the code to be profiled. Can throw exceptions.
    * @param exceptionHandler Callback for handling exceptions.
    * @return a list of places where objects were allocated.
    */
   public List<AllocationRecord> recordAllocations(RunnableThatThrows runnable, ExceptionHandler exceptionHandler)
   {
      startRecordingAllocations();

      try
      {
         runnable.run();
      }
      catch (Throwable e)
      {
         exceptionHandler.handleException(e);
      }

      stopRecordingAllocations();

      return pollAllocations();
   }

   /**
    * This method will check if the {@link AllocationRecorder} has an instrumentation. If this is not the case
    * the JVM was probably not started using the correct javaagent. To fix this start the JVM with the argument<br>
    * {@code -javaagent:[your/path/to/]java-allocation-instrumenter-3.1.0.jar}<br>
    *
    * @throws RuntimeException if no instrumentation exists or check for instrumentation failed.
    */
   public static void checkInstrumentation()
   {
      try
      {
         Method method = AllocationRecorder.class.getDeclaredMethod("getInstrumentation");
         method.setAccessible(true);
         if (method.invoke(null) == null)
         {
            throw new RuntimeException(AllocationRecorder.class.getSimpleName() + " has no instrumentation. "
                                             + "Please add VM arg -javaagent:/path/to/java-allocation-instrumenter-X.X.X.jar");
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Method implementing functional interface {@link com.google.monitoring.runtime.instrumentation.Sampler}.
    *
    * @param count the <code>int</code> count of how many instances are being
    *         allocated.  -1 means a simple new to distinguish from a 1-element array. 0
    *         shows up as a value here sometimes; one reason is T[] toArray()-type
    *         methods that require an array type argument (see ArrayList.toArray() for
    *         example).
    * @param description the <code>String</code> descriptor of the class/primitive type
    *         being allocated.
    * @param newObject the new <code>Object</code> whose allocation we're
    *         recording.
    * @param size the size of the object being allocated.
    */
   private void sampleAllocation(int count, String description, Object newObject, long size)
   {
      if (recording)
      {
         AllocationRecord record = new AllocationRecord(description, newObject, size);

         if (isIncluded(record) && !isExcluded(record)) // excludes override inclusions
         {
            allocations.add(record);
         }
      }
   }

   /**
    * The method for determining inclusions.
    */
   private boolean isIncluded(AllocationRecord record)
   {
      boolean isIncluded = false; // initialize, only have a single return in the method

      if (includeAllAllocations) // check special include all boolean
      {
         isIncluded = true;
      }
      // do an fast search to see if this allocation was of an included class, match using `startsWith` to include nested classes
      else if (includeAllocationsOfTheseClasses.stream().anyMatch(className -> record.getAllocatedObject().getClass().getName().startsWith(className)))
      {
         isIncluded = true;
      }
      else
      {
         for (StackTraceElement traceElement : record.getStackTrace()) // check entire stack trace
         {
            // if this line of the trace contains an included qualified method
            if (includeAllocationsInsideTheseMethods.contains(traceElement.getClassName() + "." + traceElement.getMethodName()))
            {
               isIncluded = true;
            }
            // if this line of the trace contains an include keyword, return true
            else if (includeAllocationsWhoseTracesContainTheseKeywords.stream().anyMatch(keyword -> traceElement.toString().contains(keyword)))
            {
               isIncluded = true;
            }
            // if this line of the trace contains an included class or it's nested class
            else if (includeAllocationsInsideTheseClasses.stream().anyMatch(className -> traceElement.getClassName().startsWith(className)))
            {
               isIncluded = true;
            }
         }
      }

      return isIncluded;
   }

   /**
    * The method for determining exclusions.
    */
   private boolean isExcluded(AllocationRecord record)
   {
      boolean isExcluded = false; // nothing is blacklisted by default

      if (excludeAllocationsOfTheseClasses.stream().anyMatch(className -> record.getAllocatedObject().getClass().getName().startsWith(className)))
      {
         isExcluded = true;
      }
      else
      {
         for (StackTraceElement traceElement : record.getStackTrace())
         {
            // if this line of the trace contains an excluded qualified method
            if (excludeAllocationsInsideTheseMethods.contains(traceElement.getClassName() + "." + traceElement.getMethodName()))
            {
               isExcluded = true;
            }
            // if this line of the trace contains an exclude keyword, return true
            else if (excludeAllocationsWhoseTracesContainTheseKeywords.stream().anyMatch(keyword -> traceElement.toString().contains(keyword)))
            {
               isExcluded = true;
            }
            // if this line of the trace contains an excluded class or it's nested class
            else if (excludeAllocationsInsideTheseClasses.stream().anyMatch(className -> traceElement.getClassName().startsWith(className)))
            {
               isExcluded = true;
            }
         }
      }

      return isExcluded;
   }

   /**
    * Set a special boolean that makes the include filter include everything.
    *
    * @param includeAllAllocations
    */
   public void setIncludeAllAllocations(boolean includeAllAllocations)
   {
      this.includeAllAllocations = includeAllAllocations;
   }

   /**
    * Include allocations from inside a class or resulting from something happening
    * inside the class, but not allocations of the class itself. (Unless the class
    * were to allocate itself)
    *
    * @param className
    */
   public void includeAllocationsInsideClass(String className)
   {
      setIncludeAllAllocations(false); // otherwise this method would do nothing
      includeAllocationsInsideTheseClasses.add(className);
   }

   /**
    * Exclude allocations from inside a class or resulting from something happening
    * inside the class, but not allocations of the class itself. (Unless the class
    * were to allocate itself)
    *
    * @param className
    */
   public void excludeAllocationsInsideClass(String className)
   {
      excludeAllocationsInsideTheseClasses.add(className);
   }

   /**
    * Include allocations of a class that happen anywhere. (i.e. new ClassName())
    *
    * @param className
    */
   public void includeAllocationsOfClass(String className)
   {
      setIncludeAllAllocations(false); // otherwise this method would do nothing
      includeAllocationsOfTheseClasses.add(className);
   }

   /**
    * Exclude allocations of a class that happen anywhere. (i.e. new ClassName())
    *
    * @param className
    */
   public void excludeAllocationsOfClass(String className)
   {
      excludeAllocationsOfTheseClasses.add(className);
   }

   /**
    * Include allocations happening inside this method or resulting from this method.
    *
    * @param qualifiedMethodName
    */
   public void includeAllocationsInsideMethod(String qualifiedMethodName)
   {
      setIncludeAllAllocations(false); // otherwise this method would do nothing
      includeAllocationsInsideTheseMethods.add(qualifiedMethodName);
   }

   /**
    * Exclude allocations happening inside this method or resulting from this method.
    *
    * @param qualifiedMethodName
    */
   public void excludeAllocationsInsideMethod(String qualifiedMethodName)
   {
      excludeAllocationsInsideTheseMethods.add(qualifiedMethodName);
   }

   /**
    * Include allocations whose stack trace contain this keyword. Useful for including
    * a package or interface methods.
    *
    * @param keyword
    */
   public void includeAllocationsContainingKeyword(String keyword)
   {
      setIncludeAllAllocations(false); // otherwise this method would do nothing
      includeAllocationsWhoseTracesContainTheseKeywords.add(keyword);
   }

   /**
    * Exclude allocations whose stack trace contain this keyword. Useful for excluding
    * a package or interface methods.
    *
    * @param keyword
    */
   public void excludeAllocationsContainingKeyword(String keyword)
   {
      excludeAllocationsWhoseTracesContainTheseKeywords.add(keyword);
   }

   /**
    * Set whether allocations inside constructors are recorded. Usually, constructors
    * are designed to allocate stuff and aren't of interest.
    *
    * @param recordConstructorAllocations
    */
   public void setRecordConstructorAllocations(boolean recordConstructorAllocations)
   {
      if (recordConstructorAllocations)
         excludeAllocationsWhoseTracesContainTheseKeywords.remove("<init>");
      else
         excludeAllocationsWhoseTracesContainTheseKeywords.add("<init>");
   }

   /**
    * Set whether to record static member initialization. Usually, you would not normally care
    * about this, but maybe you want to make sure your app has no static member initialization.
    *
    * @param recordStaticMemberInitialization
    */
   public void setRecordStaticMemberInitialization(boolean recordStaticMemberInitialization)
   {
      if (recordStaticMemberInitialization)
         excludeAllocationsWhoseTracesContainTheseKeywords.remove("<clinit>");
      else
         excludeAllocationsWhoseTracesContainTheseKeywords.add("<clinit>");
   }

   /**
    * Set whether to record the class loader loading classes. Sometimes you might not care about
    * this because classes are only loaded once.
    *
    * @param recordClassLoader
    */
   public void setRecordClassLoader(boolean recordClassLoader)
   {
      if (recordClassLoader)
         excludeAllocationsInsideTheseClasses.remove(ClassLoader.class.getName());
      else
         excludeAllocationsInsideTheseClasses.add(ClassLoader.class.getName());
   }

   /**
    * Set whether to record allocations resulting from this class or it's infrastructure.
    * Most of the time you would not want this.
    *
    * @param recordSelf
    */
   public void setRecordSelf(boolean recordSelf)
   {
      if (recordSelf)
      {
         excludeAllocationsInsideTheseClasses.remove(AllocationRecorder.class.getName());
         excludeAllocationsInsideTheseMethods.remove("us.ihmc.commons.allocations.AllocationProfiler.startRecordingAllocations");
         excludeAllocationsInsideTheseMethods.remove("java.util.concurrent.FutureTask.awaitDone");
         excludeAllocationsWhoseTracesContainTheseKeywords.remove("org.gradle.internal");
      }
      else
      {
         excludeAllocationsInsideTheseClasses.add(AllocationRecorder.class.getName());
         excludeAllocationsInsideTheseMethods.add("us.ihmc.commons.allocations.AllocationProfiler.startRecordingAllocations");
         excludeAllocationsInsideTheseMethods.add("java.util.concurrent.FutureTask.awaitDone");
         excludeAllocationsWhoseTracesContainTheseKeywords.add("org.gradle.internal");
      }
   }

   /**
    * <p>Resets this profiler, clearing all user settings and queues, stopping recording, and resetting to defaults:</p>
    *
    * <p>Record all allocations EXCEPT:</p>
    * <li>Do not record any allocations inside constructors</li>
    * <li>Do not record any allocations from and resulting from static member initialization</li>
    * <li>Do not record anything as a result of the ClassLoader loading classes</li>
    * <li>Do not record any allocations made by this profiler, Gradle, or JUnit</li>
    */
   public void reset()
   {
      if (recording)
         stopRecordingAllocations();

      includeAllAllocations = true;

      excludeAllocationsInsideTheseMethods.clear();
      includeAllocationsInsideTheseMethods.clear();
      excludeAllocationsInsideTheseClasses.clear();
      includeAllocationsInsideTheseClasses.clear();
      excludeAllocationsOfTheseClasses.clear();
      includeAllocationsOfTheseClasses.clear();
      includeAllocationsWhoseTracesContainTheseKeywords.clear();
      excludeAllocationsWhoseTracesContainTheseKeywords.clear();

      setRecordConstructorAllocations(false);
      setRecordStaticMemberInitialization(false);
      setRecordClassLoader(false);
      setRecordSelf(false);

      allocations.clear();
   }
}
