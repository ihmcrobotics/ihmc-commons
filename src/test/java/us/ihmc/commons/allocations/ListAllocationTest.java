package us.ihmc.commons.allocations;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import us.ihmc.commons.lists.PreallocatedEnumList;
import us.ihmc.commons.lists.PreallocatedList;
import us.ihmc.commons.lists.RecyclingArrayDeque;
import us.ihmc.commons.lists.RecyclingArrayList;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

public class ListAllocationTest
{
   private AllocationProfiler allocationProfiler;

   @Before
   public void setUp()
   {
      allocationProfiler = new AllocationProfiler();
      allocationProfiler.setIncludeAllAllocations(false);
      allocationProfiler.includeAllocationsInsideClass(MutableInt.class.getName());
      allocationProfiler.includeAllocationsInsideClass(RecyclingArrayList.class.getName());
      allocationProfiler.includeAllocationsInsideClass(RecyclingArrayDeque.class.getName());
      allocationProfiler.includeAllocationsInsideClass(PreallocatedList.class.getName());
      allocationProfiler.includeAllocationsInsideClass(PreallocatedEnumList.class.getName());
   }

   @Test(timeout = 30000)
   public void testRecyclingArrayList()
   {
      int capacity = 5;
      RecyclingArrayList<MutableInt> list = new RecyclingArrayList<>(capacity, MutableInt::new);
      testInternal(() ->
                   {
                      for (int i = 0; i < capacity; i++)
                      {
                         list.add().setValue(i);
                      }

                      for (int i = 0; i < capacity; i++)
                      {
                         list.get(i).setValue(2 * i);
                      }

                      for (int i = capacity - 1; i >= 0; i--)
                      {
                         list.remove(i);
                      }

                      list.clear();
                   });
   }

   @Test(timeout = 30000)
   public void testRecyclingArrayDeque()
   {
      int capacity = 8;
      RecyclingArrayDeque<MutableInt> deque = new RecyclingArrayDeque<>(capacity, MutableInt::new, MutableInt::setValue);
      MutableInt spareInt = new MutableInt();

      testInternal(() ->
                   {
                      for (int i = 0; i < 2; i++)
                      {
                         // add the exact capacity
                         deque.addFirst();
                         deque.addLast();
                         deque.add(spareInt);
                         deque.addFirst(spareInt);
                         deque.addLast(spareInt);
                         deque.push(spareInt);
                         deque.offerFirst(spareInt);
                         deque.offerLast(spareInt);

                         // remove
                         deque.removeFirst();
                         deque.removeLast();
                         deque.pollFirst();
                         deque.pollLast();
                         deque.remove();
                         deque.poll();
                         deque.pop();
                         deque.clear();
                      }
                   });
   }

   @Test(timeout = 30000)
   public void testPreallocatedList()
   {
      int capacity = 8;
      PreallocatedList<MutableInt> list = new PreallocatedList<>(MutableInt.class, MutableInt::new, capacity);

      testInternal(() ->
                   {
                      for (int i = 0; i < capacity; i++)
                      {
                         list.add();
                      }

                      for (int i = 0; i < capacity; i++)
                      {
                         list.get(i).setValue(i);
                      }

                      for (int i = 0; i < capacity - 3; i++)
                      {
                         list.remove();
                      }

                      list.clear();
                   });
   }

   @Test(timeout = 30000)
   public void testPreallocatedEnumList()
   {
      int capacity = 8;
      TestEnum[] values = TestEnum.values();

      PreallocatedEnumList<TestEnum> list = new PreallocatedEnumList<>(TestEnum.class, values, capacity);

      testInternal(() ->
                   {
                      // test adding
                      for (int i = 0; i < capacity; i++)
                      {
                         list.add(values[i % values.length]);
                      }

                      // test getting
                      for (int i = 0; i < capacity; i++)
                      {
                         list.get(i);
                      }

                      // test setting
                      for (int i = 0; i < capacity; i++)
                      {
                         list.setEnum(i, values[(i + 2) % values.length]);
                      }

                      // test removing
                      for (int i = 0; i < capacity; i++)
                      {
                         list.remove();
                      }

                      for (int i = 0; i < capacity; i++)
                      {
                         list.add(values[i % values.length]);
                      }

                      // test removing index
                      list.remove(capacity / 2);

                      // test clearing
                      list.clear();
                   });
   }

   private enum TestEnum
   {
      A, B, C, D;
   }

   private void testInternal(Runnable whatToTestFor)
   {
      List<AllocationRecord> allocations = allocationProfiler.recordAllocations(whatToTestFor);

      if (!allocations.isEmpty())
      {
         allocations.forEach(it -> System.out.println(it.toString()));
         fail("Found allocations.");
      }
   }
}
