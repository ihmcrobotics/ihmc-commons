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

   /**
    * Gets a stack trace of what created the allocation but excluding all the
    * instrumentation and recording stuff that gets thrown in afterwards, because
    * we are generating our own stack trace here.
    *
    * @return relevant stack trace
    */
   private StackTraceElement[] getCleanedStackTace()
   {
      List<StackTraceElement> stackTrace = Arrays.asList(Thread.currentThread().getStackTrace());
      int skip = 0;
      while (!stackTrace.get(skip).toString().contains(AllocationRecorder.class.getName()))
      {
         skip++;
      }
      while (stackTrace.get(skip).toString().contains(AllocationRecorder.class.getName()))
      {
         skip++;
      }
      return stackTrace.subList(skip, stackTrace.size()).toArray(new StackTraceElement[0]); // cut to the part we want
   }

   /**
    * Create the equivalent of a {@link Throwable#printStackTrace()} for this allocation.
    *
    * @return a human readable multiline print out
    */
   public String toString()
   {
      String s = "AllocationRecord: " + description + " : " + allocatedObject.getClass().getName() + " : size " + size + "\n";
      for (StackTraceElement e : stackTrace)
      {
         s += "\tat " + e.toString() + "\n";
      }
      return s;
   }

   /**
    * The <code>String</code> descriptor of the class/primitive type being allocated.
    *
    * @return description
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * The new <code>Object</code> whose allocation we're recording.
    *
    * @return allocatedObject
    */
   public Object getAllocatedObject()
   {
      return allocatedObject;
   }

   /**
    * The size of the object being allocated.
    *
    * @return size
    */
   public long getSize()
   {
      return size;
   }

   /**
    * Stack trace leading up to the allocation event.
    *
    * @return stackTrace
    */
   public StackTraceElement[] getStackTrace()
   {
      return stackTrace;
   }
}
