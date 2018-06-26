package us.ihmc.commons.allocations;

import java.util.Collections;
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
   public void testSingleAllocationClassBlacklist()
   {
      List<AllocationRecord> allocations;
      AllocationProfiler allocationProfiler = new AllocationProfiler();

      allocationProfiler.setRecordConstructorAllocations(true);
      allocations = allocationProfiler.recordAllocations(() -> new LilAllocator());
      printAllocations(allocations);
      Assert.assertEquals(3, allocations.size());

      allocationProfiler.addClassToBlacklist(MutableInt.class.getName());
      allocations = allocationProfiler.recordAllocations(() -> new LilAllocator());
      printAllocations(allocations);
      Assert.assertEquals(1, allocations.size());
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
      private MutableInt mutableInt;
      private MutableDouble mutableDouble = new MutableDouble(); // outside constructor

      public LilAllocator()
      {
         mutableInt = new MutableInt(); // inside constructor, these both count
      }
   }
}
