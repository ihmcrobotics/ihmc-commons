package us.ihmc.commons.allocations;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import us.ihmc.commons.MutationTestFacilitator;
import us.ihmc.log.LogTools;
import us.ihmc.commons.thread.ThreadTools;

import java.util.List;

@Tag("allocation")
public class AllocationTestTest
{
   private enum MyEnum
   {
      A, B, C, D
   }

   ;

   @Execution(ExecutionMode.SAME_THREAD)
   @Test
   public void testSettingOfVector()
   {
      MutableDouble data = new MutableDouble();
      List<AllocationRecord> allocations = new AllocationProfiler().recordAllocations(() -> data.setValue(1.0));
      Assertions.assertEquals(0, allocations.size());
   }

   @SuppressWarnings("unused")
   @Execution(ExecutionMode.SAME_THREAD)
   @Test
   public void testAllocationOfArray()
   {
      List<AllocationRecord> allocations = new AllocationProfiler().recordAllocations(() -> {
         double[] someArray = new double[12];
      });

      Assertions.assertEquals(1, allocations.size());
      assertAllocationsContain(allocations, double[].class);
//      Assertions.assertTrue(allocations.get(0).getAllocatedObject().getClass().equals(double[].class));
      LogTools.info(allocations.get(0).toString());
   }

   @Execution(ExecutionMode.SAME_THREAD)
   @Test
   public void testSingleAllocation()
   {
      List<AllocationRecord> allocations = new AllocationProfiler().recordAllocations(() -> new MutableDouble());
      printAllocations(allocations);
      Assertions.assertEquals(1, allocations.size());
      Assertions.assertTrue(allocations.get(0).getAllocatedObject().getClass().equals(MutableDouble.class));
      LogTools.info(allocations.get(0).toString());
   }

   @Execution(ExecutionMode.SAME_THREAD)
   @Test
   public void testSingleAllocationConstructorFilter()
   {
      List<AllocationRecord> allocations;
      AllocationProfiler allocationProfiler = new AllocationProfiler();

      allocationProfiler.setRecordConstructorAllocations(true);
      allocations = allocationProfiler.recordAllocations(() -> new LilAllocator());
      printAllocations(allocations);
      Assertions.assertEquals(3, allocations.size());

      allocationProfiler.setRecordConstructorAllocations(false);
      allocations = allocationProfiler.recordAllocations(() -> new LilAllocator());
      printAllocations(allocations);
      Assertions.assertEquals(1, allocations.size());
   }

   @Execution(ExecutionMode.SAME_THREAD)
   @Test
   public void testWhitelist()
   {
      List<AllocationRecord> allocations;
      AllocationProfiler allocationProfiler = new AllocationProfiler();

      LilAllocator lilAllocator;
      MutableInt mutableInt;

      // test control; no black or white lists
      allocationProfiler.setIncludeAllAllocations(false); // exclude everything
      allocationProfiler.startRecordingAllocations();
      lilAllocator = new LilAllocator(); // allocates 1 but ignore, stuff inside does not count
      mutableInt = new MutableInt(); // random new thing
      lilAllocator.doStuff(); // allocates 2 things inside
      allocationProfiler.stopRecordingAllocations();

      allocations = allocationProfiler.pollAllocations();
      printAllocations(allocations);
      Assertions.assertEquals(0, allocations.size()); // nothing was included so should be 0

      // add one class to whitelist
      allocationProfiler.includeAllocationsInsideClass(LilAllocator.class.getName());
      allocationProfiler.startRecordingAllocations();
      lilAllocator = new LilAllocator(); // does not count, constructor exclusion
      mutableInt = new MutableInt(); // random new thing, but not in whitelist
      lilAllocator.doStuff(); // allocates 2 things inside
      allocationProfiler.stopRecordingAllocations();

      allocations = allocationProfiler.pollAllocations();
      printAllocations(allocations);
      Assertions.assertEquals(2, allocations.size());
   }

   @Execution(ExecutionMode.SAME_THREAD)
   @Test
   public void testBlacklist()
   {
      List<AllocationRecord> allocations;
      AllocationProfiler allocationProfiler = new AllocationProfiler();
      allocationProfiler.setRecordConstructorAllocations(false);

      LilAllocator lilAllocator;

      // add one class to whitelist
      allocationProfiler.excludeAllocationsInsideClass(LilAllocator.class.getName());
      allocationProfiler.startRecordingAllocations();

      lilAllocator = new LilAllocator(); // allocates 1, stuff inside does not count

      if (lilAllocator != null)
      {
         ThreadTools.sleep(10); // otherwise apparently MutableInt will get allocated early and fail the test
         new MutableInt(); // random new thing, but not in whitelist
      }

      lilAllocator.doStuff(); // allocates 2 things inside
      allocationProfiler.stopRecordingAllocations();

      allocations = allocationProfiler.pollAllocations();
      printAllocations(allocations);

      assertAllocationsContain(allocations, LilAllocator.class);
      assertAllocationsContain(allocations, MutableInt.class);
//      Assertions.assertTrue(allocations.get(0).getAllocatedObject().getClass().equals(LilAllocator.class));
//      Assertions.assertTrue(allocations.get(0).getDescription().equals("us/ihmc/commons/allocations/AllocationTestTest$LilAllocator"));
//      Assertions.assertTrue(allocations.get(0).getSize() == 24);
//      Assertions.assertTrue(allocations.get(1).getAllocatedObject().getClass().equals(MutableInt.class));
      Assertions.assertEquals(2, allocations.size());
   }

   private void assertAllocationsContain(List<AllocationRecord> allocations, Class clazz)
   {
      boolean found = false;
      for (AllocationRecord record : allocations)
      {
         if (record.getAllocatedObject().getClass().equals(clazz))
            found = true;
      }

      Assertions.assertTrue(found);
   }

   @Execution(ExecutionMode.SAME_THREAD)
   @Test
   public void testMethodIncludeExclude()
   {
      List<AllocationRecord> allocations;
      AllocationProfiler allocationProfiler = new AllocationProfiler();

      LilAllocator lilAllocator;
      MutableInt mutableInt;

      // add one class to whitelist
      String qualifiedMethodName = "us.ihmc.commons.allocations.AllocationTestTest$BrokenClass.imNotSupposedToAllocate";
      allocationProfiler.includeAllocationsInsideMethod(qualifiedMethodName);
      allocationProfiler.startRecordingAllocations();
      lilAllocator = new LilAllocator(); // allocates 1, stuff inside does not count
      mutableInt = new MutableInt(); // random new thing, but not in whitelist
      lilAllocator.doStuff(); // allocates 2 things inside
      allocationProfiler.stopRecordingAllocations();

      allocations = allocationProfiler.pollAllocations();
      LogTools.info("testMethodIncludeExclude1");
      printAllocations(allocations);
      Assertions.assertTrue(allocations.get(0).toString().contains(qualifiedMethodName));
      Assertions.assertEquals(1, allocations.size());

      // add one class to whitelist
      allocationProfiler.setIncludeAllAllocations(true);
      allocationProfiler.excludeAllocationsInsideMethod(qualifiedMethodName);
      allocationProfiler.startRecordingAllocations();
      lilAllocator = new LilAllocator(); // allocates 1, stuff inside does not count
      mutableInt = new MutableInt(); // random new thing, but not in whitelist
      lilAllocator.doStuff(); // allocates 2 things inside, but one excluded
      allocationProfiler.stopRecordingAllocations();

      allocations = allocationProfiler.pollAllocations();
      LogTools.info("testMethodIncludeExclude2");
      printAllocations(allocations);
      Assertions.assertEquals(3, allocations.size());
   }

   @Execution(ExecutionMode.SAME_THREAD)
   @Test
   public void testReset()
   {
      List<AllocationRecord> allocations;
      AllocationProfiler allocationProfiler = new AllocationProfiler();
      LilAllocator lilAllocator;
      MutableInt mutableInt;

      String qualifiedMethodName = "us.ihmc.commons.allocations.AllocationTestTest$BrokenClass.imNotSupposedToAllocate";
      allocationProfiler.includeAllocationsInsideMethod(qualifiedMethodName);
      allocationProfiler.startRecordingAllocations();
      lilAllocator = new LilAllocator(); // allocates 1, stuff inside does not count
      mutableInt = new MutableInt(); // random new thing, but not in whitelist
      lilAllocator.doStuff(); // allocates 2 things inside
      allocationProfiler.stopRecordingAllocations();
      allocations = allocationProfiler.pollAllocations();
      printAllocations(allocations);
      Assertions.assertTrue(allocations.get(0).toString().contains(qualifiedMethodName));
      Assertions.assertEquals(1, allocations.size());

      allocationProfiler.reset();
      allocationProfiler.startRecordingAllocations();
      lilAllocator = new LilAllocator(); // allocates 1, stuff inside does not count
      mutableInt = new MutableInt(); // random new thing, but not in whitelist
      lilAllocator.doStuff(); // allocates 2 things inside, but one excluded
      allocationProfiler.stopRecordingAllocations();
      allocations = allocationProfiler.pollAllocations();
      printAllocations(allocations);
      Assertions.assertEquals(4, allocations.size());
   }

   private void printAllocations(List<AllocationRecord> allocations)
   {
      for (AllocationRecord allocation : allocations)
      {
         LogTools.info(allocation.toString());
      }
   }

   // this switch doesn't allocate when run with Gradle. It therefore no longer reliably tests the application logic so disable it. - @dcalvert
   @Disabled
   @Execution(ExecutionMode.SAME_THREAD)
   @Test
   public void testSwitchTable()
   {
      // First time the switch statement is called for an enum a switch table is generated:
      List<AllocationRecord> allocations = new AllocationProfiler().recordAllocations(() -> {
         switch (MyEnum.A)
         {
         default:
            break;
         }
      });
      printAllocations(allocations);
      Assertions.assertEquals(2, allocations.size(), "allocated");

      // The second time there are no allocations:
      allocations = new AllocationProfiler().recordAllocations(() -> {
         switch (MyEnum.B)
         {
         default:
            break;
         }
      });
      printAllocations(allocations);
      Assertions.assertEquals(0, allocations.size(), "allocated");
   }

   private class LilAllocator
   {
      private BrokenClass brokenClass; // "class of interest"
      private KnownAllocator knownAllocator = new KnownAllocator(); // outside constructor

      public LilAllocator()
      {
         brokenClass = new BrokenClass(); // inside constructor, these both count
      }

      public void doStuff()
      {
         brokenClass.imNotSupposedToAllocate();
         knownAllocator.whoCaresIfIAllocate();
      }
   }

   private class BrokenClass
   {
      public MutableInt mutableInt;

      public void imNotSupposedToAllocate()
      {
         mutableInt = new MutableInt();
      }
   }

   private class KnownAllocator
   {
      public MutableInt mutableInt;

      public void whoCaresIfIAllocate()
      {
         mutableInt = new MutableInt();
      }
   }

   public static void main(String[] args)
   {
      MutationTestFacilitator.facilitateMutationTestForClass(AllocationProfiler.class, AllocationTestTest.class);
   }
}
