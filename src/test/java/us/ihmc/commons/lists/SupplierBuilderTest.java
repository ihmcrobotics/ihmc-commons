package us.ihmc.commons.lists;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.IntFunction;
import java.util.function.Supplier;

public class SupplierBuilderTest
{
   @Test(timeout = 30000)
   public void testZeroIndexedSupplierBuilder()
   {
      IntFunction<MutableInt> indexFunction = (index) -> new MutableInt(2 * index - 1);
      Supplier<MutableInt> intSupplier = SupplierBuilder.indexedSupplier(indexFunction);

      for (int i = 0; i < 10; i++)
      {
         MutableInt mutableInt = intSupplier.get();
         Assert.assertEquals((long) mutableInt.getValue(), (long) indexFunction.apply(i).getValue());
      }
   }

   @Test(timeout = 30000)
   public void testNonZeroIndexedSupplierBuilder()
   {
      int initialIndex = 10;
      IntFunction<MutableInt> indexFunction = (index) -> new MutableInt(-3 * index + 1);
      Supplier<MutableInt> intSupplier = SupplierBuilder.indexedSupplier(indexFunction, initialIndex);

      for (int i = 0; i < 10; i++)
      {
         MutableInt mutableInt = intSupplier.get();
         Assert.assertEquals((long) mutableInt.getValue(), (long) indexFunction.apply(i + initialIndex).getValue());
      }
   }
}
