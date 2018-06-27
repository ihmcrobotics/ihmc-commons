package us.ihmc.commons.allocations;

import com.google.monitoring.runtime.instrumentation.AllocationRecorder;
import us.ihmc.commons.PrintTools;
import us.ihmc.commons.RunnableThatThrows;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.exception.ExceptionHandler;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AllocationProfiler
{
   private final Set<String> methodBlacklist = new HashSet<>();
   private final Set<String> methodWhitelist = new HashSet<>();
   private final Set<String> classBlacklist = new HashSet<>();
   private final Set<String> classWhitelist = new HashSet<>();

   private final Queue<AllocationRecord> allocations = new ConcurrentLinkedQueue<>();

   private boolean stopped = true;

   private boolean recordConstructorAllocations;
   private boolean recordStaticMemberInitialization;

   public AllocationProfiler()
   {
      setRecordConstructorAllocations(true);
      setRecordStaticMemberInitialization(true);
      setRecordClassLoader(false);
      setRecordSelf(false);
   }

   public void addMethodToBlacklist(String methodName)
   {
      methodBlacklist.add(methodName);
   }

   public void addMethodToWhitelist(String methodName)
   {
      methodWhitelist.add(methodName);
   }

   public void addClassToBlacklist(String className)
   {
      classBlacklist.add(className);
   }

   public void addClassToWhitelist(String className)
   {
      classWhitelist.add(className);
   }

   public void setRecordConstructorAllocations(boolean recordConstructorAllocations)
   {
      this.recordConstructorAllocations = recordConstructorAllocations;
   }

   public void setRecordStaticMemberInitialization(boolean staticMemberInitialization)
   {
      this.recordStaticMemberInitialization = recordStaticMemberInitialization;
   }

   public void setRecordClassLoader(boolean recordClassLoader)
   {
      if (recordClassLoader)
         classBlacklist.remove(ClassLoader.class.getName());
      else
         classBlacklist.add(ClassLoader.class.getName());
   }

   public void setRecordSelf(boolean recordSelf)
   {
      if (recordSelf)
         classBlacklist.remove(AllocationRecorder.class.getName());
      else
         classBlacklist.add(AllocationRecorder.class.getName());
   }

   public List<AllocationRecord> pollAllocations()
   {
      return removeDuplicateRecords(pollAllocationsIncludingDuplicates());
   }

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

   public void startRecordingAllocations()
   {
      checkInstrumentation();

      stopped = false;
      AllocationRecorder.addSampler(this::sampleAllocation);
   }

   public void stopRecordingAllocations()
   {
      stopped = true;
      AllocationRecorder.removeSampler(this::sampleAllocation);
   }

   /**
    * Will run the provided runnable and return a list of places where allocations occurred.
    * If the returned list is empty no allocations where detected.
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
            throw new RuntimeException(AllocationRecorder.class.getSimpleName() + " has no instrumentation.");
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
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

   private void sampleAllocation(int count, String description, Object newObject, long size)
   {
      if (stopped)
      {
         return;
      }

      if (description.equals("com/google/monitoring/runtime/instrumentation/Sampler"))
      {
         return; // this happens when samplers get added and removed
      }

      AllocationRecord allocation = new AllocationRecord(description, newObject, size);

      // Skip static member initializations.
      if (!recordStaticMemberInitialization && allocation.getStackTrace()[0].getMethodName().contains("<clinit>"))
      {
         return;
      }
      // Skip things inside constructors.
      if (!recordConstructorAllocations && allocation.getStackTrace()[0].getMethodName().contains("<init>"))
      {
         return;
      }

      if (!passesWhiteFilter(allocation))
      {
         return;
      }
      if (!passesBlackFilter(allocation))
      {
         return;
      }

      //      PrintTools.debug(this, description);

      allocations.add(allocation);
   }

   private boolean passesWhiteFilter(AllocationRecord record)
   {
      boolean passes = false; // exclude everything

      if (methodWhitelist.isEmpty() && classWhitelist.isEmpty()) // special case if both whitelists are empty
      {
         passes = true; // allow everything
      }
      else
      {
         traceloop:
         for (StackTraceElement traceElement : record.getStackTrace()) // check entire stack trace
         {
            String qualifiedMethodName = traceElement.getClassName() + "." + traceElement.getMethodName();
            if (methodWhitelist.contains(qualifiedMethodName)) // allow methods from whitelist
            {
               PrintTools.debug(this, "WHITE: qualifiedMethodName: " + record.toString());
               passes = true;
               break traceloop;
            }
            else
            {
               for (String className : classWhitelist)
               {
//                  if (record.getNewObject().getClass().getName().startsWith(className) // the whitelisted class itself or it's subclass got allocated
//                        || traceElement.getClassName().startsWith(className)) // some allocation in side this class or it's subclass
                  if (traceElement.getClassName().startsWith(className)) // some allocation in side this class or it's subclass
                  {
                     PrintTools.debug(this, "WHITE: className: " + record.toString());
                     passes = true;
                     break traceloop;
                  }
               }
            }
         }
      }

      //      PrintTools.debug(this, "Whitelisted: " + passes + " : " + record.getDescription());

      return passes;
   }

   private boolean passesBlackFilter(AllocationRecord record)
   {
      boolean passes = true; // nothing is blacklisted by default

      if (methodBlacklist.isEmpty() && classBlacklist.isEmpty()) // optimization check, nothing to block
      {
         passes = true; // don't bother checking anything
      }
      else
      {
         traceloop:
         for (StackTraceElement traceElement : record.getStackTrace())
         {
            String qualifiedMethodName = traceElement.getClassName() + "." + traceElement.getMethodName();
            if (methodBlacklist.contains(qualifiedMethodName))
            {
               passes = false;
               break traceloop;
            }
            else
            {
               for (String className : classBlacklist)
               {
                  if (traceElement.getClassName().startsWith(className))
                  {
                     passes = false;
                     break traceloop;
                  }
               }
            }
         }
      }

      //      PrintTools.debug(this, "Blacklisted: " + passes + " : " + record.toString());

      return passes;
   }
}
