package us.ihmc.commons.allocations;

import com.google.monitoring.runtime.instrumentation.AllocationRecorder;

import java.util.Arrays;
import java.util.List;

public class AllocationRecord
{
   private final String description;
   private final Object newObject;
   private final long size;

   private final StackTraceElement[] stackTrace;

   public AllocationRecord(String description, Object newObject, long size)
   {
      this.description = description;
      this.newObject = newObject;
      this.size = size;

      stackTrace = getCleanedStackTace();
   }

   private StackTraceElement[] getCleanedStackTace()
   {
      String recordClass = AllocationRecorder.class.getName();
      List<StackTraceElement> stackTrace = Arrays.asList(Thread.currentThread().getStackTrace());
      int skip = 0;
      while (!stackTrace.get(skip).toString().contains(recordClass))
      {
         skip++;
      }
      while (stackTrace.get(skip).toString().contains(recordClass))
      {
         skip++;
      }
      return stackTrace.subList(skip, stackTrace.size()).toArray(new StackTraceElement[0]);
   }

   public String toString()
   {
      String s = "AllocationRecord: " + description + " : " + newObject.getClass().getName() + "\n";
      for (StackTraceElement e : stackTrace)
      {
         s += "\tat " + e.toString() + "\n";
      }
      return s;
   }

   public String getDescription()
   {
      return description;
   }

   public Object getNewObject()
   {
      return newObject;
   }

   public long getSize()
   {
      return size;
   }

   public StackTraceElement[] getStackTrace()
   {
      return stackTrace;
   }
}
