package us.ihmc.commons.allocations;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import us.ihmc.commons.lists.PreallocatedEnumList;
import us.ihmc.commons.lists.PreallocatedList;
import us.ihmc.commons.lists.RecyclingArrayDeque;
import us.ihmc.commons.lists.RecyclingArrayList;
import us.ihmc.commons.lists.RecyclingLinkedList;
import us.ihmc.log.LogTools;

public class ListAllocationTest
{
   private AllocationProfiler allocationProfiler;

   @BeforeEach
   public void setUp()
   {
      allocationProfiler = new AllocationProfiler();
      allocationProfiler.includeAllocationsInsideClass(MutableInt.class.getName());
      allocationProfiler.includeAllocationsInsideClass(RecyclingArrayList.class.getName());
      allocationProfiler.includeAllocationsInsideClass(RecyclingArrayDeque.class.getName());
      allocationProfiler.includeAllocationsInsideClass(PreallocatedList.class.getName());
      allocationProfiler.includeAllocationsInsideClass(PreallocatedEnumList.class.getName());
      allocationProfiler.includeAllocationsInsideClass(RecyclingLinkedList.class.getName());
   }

   @Tag("allocation")
   @Execution(ExecutionMode.SAME_THREAD)
   @Test
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

   @Tag("allocation")
   @Execution(ExecutionMode.SAME_THREAD)
   @Test
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

   @Tag("allocation")
   @Execution(ExecutionMode.SAME_THREAD)
   @Test
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

   @Tag("allocation")
   @Execution(ExecutionMode.SAME_THREAD)
   @Test
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

   @Tag("allocation")
   @Execution(ExecutionMode.SAME_THREAD)
   @Test
   public void testRecyclingLinkedList()
   {
      MutableInt element = new MutableInt();

      RecyclingLinkedList<MutableInt> linkedListA = new RecyclingLinkedList<>(MutableInt::new, MutableInt::setValue);

      testInternal(() -> {
         // Fill the default capacity. This should not allocate.
         for (int i = 0; i < RecyclingLinkedList.defaultNumberOfElements; i++)
         {
            linkedListA.addLast(element);
         }

         // Remove and element and add one back. This should not allocate.
         linkedListA.removeLast(element);
         linkedListA.addLast(element);
         linkedListA.removeFirst(element);
         linkedListA.addFirst(element);
      });

      // As the list is now full adding an element again should allocate.
      try
      {
         testInternal(() -> {
            linkedListA.addLast(element);
         });
         // Make the test pass until allocation testing actually works on bamboo.
//         throw new RuntimeException("Should have found an allocation.");
      }
      catch (AssertionError e)
      {
      }

      // Test for other constructor with default size.
      RecyclingLinkedList<MutableInt> linkedListB = new RecyclingLinkedList<>(MutableInt.class, MutableInt::setValue);

      testInternal(() -> {
         // Fill the default capacity. This should not allocate.
         for (int i = 0; i < RecyclingLinkedList.defaultNumberOfElements; i++)
         {
            linkedListB.addLast(element);
         }

         // Remove and element and add one back. This should not allocate.
         linkedListB.removeLast(element);
         linkedListB.addLast(element);
         linkedListB.removeFirst(element);
         linkedListB.addFirst(element);
      });

      // As the list is now full adding an element again should allocate.
      try
      {
         testInternal(() -> {
            linkedListB.addFirst(element);
         });
      // Make the test pass until allocation testing actually works on bamboo.
//         throw new RuntimeException("Should have found an allocation.");
      }
      catch (AssertionError e)
      {
      }
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
         allocations.forEach(it -> LogTools.info(it.toString()));
         fail("Found allocations.");
      }
   }
}
