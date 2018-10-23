package us.ihmc.commons.lists;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.IntFunction;
import java.util.function.Supplier;

public class SupplierBuilderTest
{
   @Test
   public void testZeroIndexedSupplierBuilder()
   {
      IntFunction<MutableInt> indexFunction = (index) -> new MutableInt(2 * index - 1);
      Supplier<MutableInt> intSupplier = SupplierBuilder.indexedSupplier(indexFunction);

      for (int i = 0; i < 10; i++)
      {
         MutableInt mutableInt = intSupplier.get();
         assertEquals((long) mutableInt.getValue(), (long) indexFunction.apply(i).getValue());
      }
   }

   @Test
   public void testNonZeroIndexedSupplierBuilder()
   {
      int initialIndex = 10;
      IntFunction<MutableInt> indexFunction = (index) -> new MutableInt(-3 * index + 1);
      Supplier<MutableInt> intSupplier = SupplierBuilder.indexedSupplier(indexFunction, initialIndex);

      for (int i = 0; i < 10; i++)
      {
         MutableInt mutableInt = intSupplier.get();
         assertEquals((long) mutableInt.getValue(), (long) indexFunction.apply(i + initialIndex).getValue());
      }
   }

   @Test
   public void testMutableIntSupplierFromConstructor()
   {
      Supplier<MutableInt> supplier = SupplierBuilder.createFromEmptyConstructor(MutableInt.class);
      for (int i = 0; i < 10; i++)
      {
         assertEquals((long) supplier.get().getValue(), 0);
      }
   }
}
