package us.ihmc.commons.allocations;

import com.google.monitoring.runtime.instrumentation.AllocationRecorder;

import java.util.Arrays;
import java.util.List;

/**
 * A result given by the {@link com.google.monitoring.runtime.instrumentation.Sampler} interface.
 */
public class AllocationRecord
{
   /** The <code>String</code> descriptor of the class/primitive type being allocated. */
   private final String description;

   /** The new <code>Object</code> whose allocation we're recording. */
   private final Object allocatedObject;

   /** The size of the object being allocated. */
   private final long size;

   /** A stacktrace created on construction of this class. */
   private final StackTraceElement[] stackTrace;

   /**
    * Create a new record with data given by {@link com.google.monitoring.runtime.instrumentation.Sampler}.
    *
    * @param description The <code>String</code> descriptor of the class/primitive type being allocated.
    * @param allocatedObject The new <code>Object</code> whose allocation we're recording.
    * @param size The size of the object being allocated.
    */
   public AllocationRecord(String description, Object allocatedObject, long size)
   {
      this.description = description;
      this.allocatedObject = allocatedObject;
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
      String s = "AllocationRecord: " + description + " : " + allocatedObject.getClass().getName() + " : size " + size + "\n";
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

   public Object getAllocatedObject()
   {
      return allocatedObject;
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
