package us.ihmc.commons.allocations;

import java.util.List;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Assert;
import org.junit.Test;

import us.ihmc.commons.PrintTools;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.IntegrationCategory;

public class AllocationTestTest
{
   private enum MyEnum
   {
      A, B, C, D
   };

   @ContinuousIntegrationTest(estimatedDuration = 0.0, categoriesOverride = {IntegrationCategory.SLOW})
   @Test(timeout = 3000)
   public void testSettingOfVector()
   {
      MutableDouble data = new MutableDouble();
      List<AllocationRecord> allocations = new AllocationProfiler().recordAllocations(() -> data.setValue(1.0));
      Assert.assertEquals(0, allocations.size());
   }

   @SuppressWarnings("unused")
   @ContinuousIntegrationTest(estimatedDuration = 0.0, categoriesOverride = {IntegrationCategory.SLOW})
   @Test(timeout = 3000)
   public void testAllocationOfArray()
   {
      List<AllocationRecord> allocations = new AllocationProfiler().recordAllocations(() -> {
         double[] someArray = new double[12];
      });

      Assert.assertEquals(1, allocations.size());
      Assert.assertTrue(allocations.get(0).getNewObject().getClass().equals(double[].class));
      PrintTools.info(allocations.get(0).toString());
   }

   @ContinuousIntegrationTest(estimatedDuration = 0.0, categoriesOverride = {IntegrationCategory.SLOW})
   @Test(timeout = 3000)
   public void testSingleAllocation()
   {
      List<AllocationRecord> allocations = new AllocationProfiler().recordAllocations(() -> new MutableDouble());
      Assert.assertEquals(1, allocations.size());
      Assert.assertTrue(allocations.get(0).getNewObject().getClass().equals(MutableDouble.class));
      PrintTools.info(allocations.get(0).toString());
   }

   @ContinuousIntegrationTest(estimatedDuration = 0.0, categoriesOverride = {IntegrationCategory.SLOW})
   @Test(timeout = 3000)
   public void testSingleAllocationConstructorFilter()
   {
      List<AllocationRecord> allocations;
      AllocationProfiler allocationProfiler = new AllocationProfiler();

      allocationProfiler.setRecordConstructorAllocations(true);
      allocations = allocationProfiler.recordAllocations(() -> new LilAllocator());
      printAllocations(allocations);
      Assert.assertEquals(3, allocations.size());

      allocationProfiler.setRecordConstructorAllocations(false);
      allocations = allocationProfiler.recordAllocations(() -> new LilAllocator());
      printAllocations(allocations);
      Assert.assertEquals(1, allocations.size());
   }

   @ContinuousIntegrationTest(estimatedDuration = 0.0, categoriesOverride = {IntegrationCategory.SLOW})
   @Test(timeout = 3000)
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
      Assert.assertEquals(0, allocations.size()); // nothing was included so should be 0

      // add one class to whitelist
      allocationProfiler.includeAllocationsInsideClass(LilAllocator.class.getName());
      allocationProfiler.startRecordingAllocations();
      lilAllocator = new LilAllocator(); // does not count, constructor exclusion
      mutableInt = new MutableInt(); // random new thing, but not in whitelist
      lilAllocator.doStuff(); // allocates 2 things inside
      allocationProfiler.stopRecordingAllocations();

      allocations = allocationProfiler.pollAllocations();
      printAllocations(allocations);
      Assert.assertEquals(2, allocations.size());
   }

   @ContinuousIntegrationTest(estimatedDuration = 0.0, categoriesOverride = {IntegrationCategory.SLOW})
   @Test(timeout = 3000)
   public void testBlacklist()
   {
      List<AllocationRecord> allocations;
      AllocationProfiler allocationProfiler = new AllocationProfiler();
      allocationProfiler.setRecordConstructorAllocations(false);

      LilAllocator lilAllocator;
      MutableInt mutableInt;

      // add one class to whitelist
      allocationProfiler.excludeAllocationsInsideClass(LilAllocator.class.getName());
      allocationProfiler.startRecordingAllocations();
      lilAllocator = new LilAllocator(); // allocates 1, stuff inside does not count
      mutableInt = new MutableInt(); // random new thing, but not in whitelist
      lilAllocator.doStuff(); // allocates 2 things inside
      allocationProfiler.stopRecordingAllocations();

      allocations = allocationProfiler.pollAllocations();
      printAllocations(allocations);
      Assert.assertTrue(allocations.get(0).getNewObject().getClass().equals(LilAllocator.class));
      Assert.assertTrue(allocations.get(1).getNewObject().getClass().equals(MutableInt.class));
      Assert.assertEquals(2, allocations.size());
   }

   @ContinuousIntegrationTest(estimatedDuration = 0.0, categoriesOverride = {IntegrationCategory.SLOW})
   @Test(timeout = 3000)
   public void testMethodIncludeExclude()
   {
      List<AllocationRecord> allocations;
      AllocationProfiler allocationProfiler = new AllocationProfiler();

      LilAllocator lilAllocator;
      MutableInt mutableInt;

      // add one class to whitelist
      allocationProfiler.setIncludeAllAllocations(false);
      String qualifiedMethodName = "us.ihmc.commons.allocations.AllocationTestTest$BrokenClass.imNotSupposedToAllocate";
      allocationProfiler.includeAllocationsInsideMethod(qualifiedMethodName);
      allocationProfiler.startRecordingAllocations();
      lilAllocator = new LilAllocator(); // allocates 1, stuff inside does not count
      mutableInt = new MutableInt(); // random new thing, but not in whitelist
      lilAllocator.doStuff(); // allocates 2 things inside
      allocationProfiler.stopRecordingAllocations();

      allocations = allocationProfiler.pollAllocations();
      printAllocations(allocations);
      Assert.assertTrue(allocations.get(0).toString().contains(qualifiedMethodName));
      Assert.assertEquals(1, allocations.size());

      // add one class to whitelist
      allocationProfiler.setIncludeAllAllocations(true);
      allocationProfiler.excludeAllocationsInsideMethod(qualifiedMethodName);
      allocationProfiler.startRecordingAllocations();
      lilAllocator = new LilAllocator(); // allocates 1, stuff inside does not count
      mutableInt = new MutableInt(); // random new thing, but not in whitelist
      lilAllocator.doStuff(); // allocates 2 things inside, but one excluded
      allocationProfiler.stopRecordingAllocations();

      allocations = allocationProfiler.pollAllocations();
      printAllocations(allocations);
      Assert.assertEquals(3, allocations.size());
   }

   private void printAllocations(List<AllocationRecord> allocations)
   {
      for (AllocationRecord allocation : allocations)
      {
         PrintTools.info(allocation.toString());
      }
   }

   @ContinuousIntegrationTest(estimatedDuration = 0.0, categoriesOverride = {IntegrationCategory.SLOW})
   @Test(timeout = 3000)
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
      Assert.assertFalse(allocations.isEmpty());

      // The second time there are no allocations:
      allocations = new AllocationProfiler().recordAllocations(() -> {
         switch (MyEnum.B)
         {
         default:
            break;
         }
      });
      Assert.assertTrue(allocations.isEmpty());
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
}
