package us.ihmc.commons.allocations;

import org.apache.commons.lang3.mutable.MutableDouble;
import us.ihmc.commons.PrintTools;
import us.ihmc.commons.lists.RecyclingArrayList;

import java.io.IOException;
import java.util.List;

/**
 * This is a demo for the {@link AllocationProfiler}.
 * <p>
 * For this to work you will need to add the following argument to your java VM arguments:<br>
 * {@code -javaagent:[your/path/to/]java-allocation-instrumenter-3.1.0.jar}<br>
 * </p>
 * @author Georg
 */
public class AllocationRecordingDemo
{
   public AllocationRecordingDemo()
   {
      AllocationProfiler allocationProfiler = new AllocationProfiler();
      allocationProfiler.includeAllocationsInsideClass(RecyclingArrayList.class.getName());

      List<AllocationRecord> allocations;

      int initialSize = 6;
      RecyclingArrayList<MutableDouble> myList = new RecyclingArrayList<>(initialSize, MutableDouble.class);

      allocationProfiler.startRecordingAllocations(); // start recording

      // This should not allocate objects since the list is large enough.
      myList.clear();
      for (int i = 0; i < initialSize; i++)
      {
         myList.add();
      }

      allocationProfiler.stopRecordingAllocations(); // stop recording

      allocations = allocationProfiler.pollAllocations(); // get results

      PrintTools.info("Number of places where allocations occured: " + allocations.size());
      allocations.forEach(allocation -> System.out.println(allocation.toString()));

      // This should allocate a new Vector3D
      allocations = allocationProfiler.recordAllocations(() -> myList.add()); // convenience method, start, run, stop, poll in one step

      PrintTools.info("Number of places where allocations occured: " + allocations.size());

      allocations.forEach(allocation -> System.out.println(allocation.toString()));
   }

   public static void main(String[] args) throws IOException
   {
      new AllocationRecordingDemo();
   }
}
