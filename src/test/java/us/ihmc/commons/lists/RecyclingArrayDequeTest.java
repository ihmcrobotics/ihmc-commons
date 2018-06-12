package us.ihmc.commons.lists;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Test;

import java.util.Collections;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

public class RecyclingArrayDequeTest
{
   @Test(timeout = 30000)
   public void testConstructors()
   {
      RecyclingArrayDeque<MutableInt> queue = new RecyclingArrayDeque<MutableInt>(MutableInt::new, MutableInt::setValue);
      assertTrue(queue.isEmpty());
      assertTrue(queue.pollLast() == null);

      queue = new RecyclingArrayDeque<MutableInt>(MutableInt.class, MutableInt::setValue);
      assertTrue(queue.isEmpty());
      assertTrue(queue.pollLast() == null);

      queue = new RecyclingArrayDeque<MutableInt>(10, MutableInt.class, MutableInt::setValue);
      assertTrue(queue.isEmpty());
      assertTrue(queue.pollLast() == null);

      queue = new RecyclingArrayDeque<MutableInt>(10, MutableInt::new, MutableInt::setValue);
      assertTrue(queue.isEmpty());
      assertTrue(queue.pollLast() == null);
   }

   @Test(timeout = 30000)
   public void testAddFirst()
   {
      RecyclingArrayDeque<MutableInt> queue = new RecyclingArrayDeque<MutableInt>(4, MutableInt::new, MutableInt::setValue);

      queue.addFirst().setValue(1);
      assertTrue(queue.size() == 1);
      queue.addLast().setValue(2);
   }

   @Test(timeout = 30000)
   public void testPollFirst()
   {
      RecyclingArrayDeque<MutableInt> queue = new RecyclingArrayDeque<MutableInt>(4, MutableInt::new, MutableInt::setValue);
      queue.addLast().setValue(1);
      queue.addLast().setValue(2);
      queue.addLast().setValue(3);
      queue.addLast().setValue(4);

      assertTrue(queue.size() == 4);
      assertTrue(queue.pollFirst().getValue() == 1);
      assertTrue(queue.size() == 3);
      assertTrue(queue.pollFirst().getValue() == 2);
      assertTrue(queue.size() == 2);
      assertTrue(queue.pollFirst().getValue() == 3);
      assertTrue(queue.size() == 1);
      assertTrue(queue.pollFirst().getValue() == 4);
      assertTrue(queue.size() == 0);
   }

   @Test(timeout = 30000)
   public void testPollLast()
   {
      RecyclingArrayDeque<MutableInt> queue = new RecyclingArrayDeque<MutableInt>(4, MutableInt::new, MutableInt::setValue);
      queue.addFirst().setValue(2);
      queue.addFirst().setValue(1);
      queue.addLast().setValue(3);
      queue.addLast().setValue(4);

      assertTrue(queue.size() == 4);
      assertTrue(queue.pollLast().getValue() == 4);
      assertTrue(queue.size() == 3);
      assertTrue(queue.pollLast().getValue() == 3);
      assertTrue(queue.size() == 2);
      assertTrue(queue.pollLast().getValue() == 2);
      assertTrue(queue.size() == 1);
      assertTrue(queue.pollLast().getValue() == 1);
      assertTrue(queue.size() == 0);
   }

   @Test(timeout = 30000)
   public void testPeek()
   {
      RecyclingArrayDeque<MutableInt> queue = new RecyclingArrayDeque<MutableInt>(4, MutableInt::new, MutableInt::setValue);
      queue.addLast().setValue(3);
      queue.addLast().setValue(4);
      queue.addFirst().setValue(2);
      queue.addFirst().setValue(1);

      assertTrue(queue.peek().getValue() == 1);
      assertTrue(queue.peek().getValue() == 1);
      assertTrue(queue.pollFirst().getValue() == 1);
      assertTrue(queue.peek().getValue() == 2);
      assertTrue(queue.size() == 3);
   }

   @Test(timeout = 30000)
   public void testPeekFirst()
   {
      //functionally identical to peek()
      RecyclingArrayDeque<MutableInt> queue = new RecyclingArrayDeque<MutableInt>(4, MutableInt::new, MutableInt::setValue);
      queue.addLast().setValue(3);
      queue.addFirst().setValue(2);
      queue.addLast().setValue(4);
      queue.addFirst().setValue(1);

      assertTrue(queue.peek().getValue() == 1);
      assertTrue(queue.peek().getValue() == 1);
      assertTrue(queue.pollFirst().getValue() == 1);
      assertTrue(queue.peek().getValue() == 2);
      assertTrue(queue.size() == 3);
   }

   @Test(timeout = 30000)
   public void testPeekLast()
   {
      RecyclingArrayDeque<MutableInt> queue = new RecyclingArrayDeque<MutableInt>(4, MutableInt::new, MutableInt::setValue);
      queue.addFirst().setValue(4);
      queue.addFirst().setValue(3);
      queue.addFirst().setValue(2);
      queue.addFirst().setValue(1);

      assertTrue(queue.peekLast().getValue() == 4);
      assertTrue(queue.peekLast().getValue() == 4);
      assertTrue(queue.pollLast().getValue() == 4);
      assertTrue(queue.peekLast().getValue() == 3);
      assertTrue(queue.size() == 3);
   }

   @Test(timeout = 30000)
   public void testRemoveFirst()
   {
      RecyclingArrayDeque<MutableInt> queue = new RecyclingArrayDeque<MutableInt>(4, MutableInt::new, MutableInt::setValue);
      queue.addFirst().setValue(4);
      queue.addFirst().setValue(3);
      queue.addFirst().setValue(2);
      queue.addFirst().setValue(1);

      assertTrue(queue.removeFirst().getValue() == 1);
      assertTrue(queue.removeFirst().getValue() == 2);
      assertTrue(queue.removeFirst().getValue() == 3);
      assertTrue(queue.removeFirst().getValue() == 4);
   }

   @Test(timeout = 30000)
   public void testRemove()
   {
      // identical to remove first
      RecyclingArrayDeque<MutableInt> queue = new RecyclingArrayDeque<MutableInt>(4, MutableInt::new, MutableInt::setValue);
      queue.addFirst().setValue(4);
      queue.addFirst().setValue(3);
      queue.addFirst().setValue(2);
      queue.addFirst().setValue(1);

      assertTrue(queue.remove().getValue() == 1);
      assertTrue(queue.remove().getValue() == 2);
      assertTrue(queue.remove().getValue() == 3);
      assertTrue(queue.remove().getValue() == 4);
   }

   @Test(timeout = 30000)
   public void testRemoveLast()
   {
      RecyclingArrayDeque<MutableInt> queue = new RecyclingArrayDeque<MutableInt>(4, MutableInt::new, MutableInt::setValue);
      queue.addFirst().setValue(4);
      queue.addFirst().setValue(3);
      queue.addFirst().setValue(2);
      queue.addFirst().setValue(1);

      assertTrue(queue.removeLast().getValue() == 4);
      assertTrue(queue.removeLast().getValue() == 3);
      assertTrue(queue.removeLast().getValue() == 2);
      assertTrue(queue.removeLast().getValue() == 1);
   }

   @Test(timeout = 30000)
   public void testUnsupportedMethods()
   {
      RecyclingArrayDeque<MutableInt> queue = new RecyclingArrayDeque<MutableInt>(4, MutableInt::new, MutableInt::setValue);
      failIfSuccessful(queue::clone);
      failIfSuccessful(() -> queue.remove(new MutableInt()));
      failIfSuccessful(() -> queue.contains(new MutableInt()));
      failIfSuccessful(queue::iterator);
      failIfSuccessful(queue::toArray);
      failIfSuccessful(() -> queue.toArray(new MutableInt[0]));
      failIfSuccessful(() -> queue.containsAll(Collections.emptyList()));
      failIfSuccessful(() -> queue.addAll(Collections.emptyList()));
      failIfSuccessful(() -> queue.removeAll(Collections.emptyList()));
      failIfSuccessful(() -> queue.retainAll(Collections.emptyList()));
      failIfSuccessful(() -> queue.removeFirstOccurrence(new MutableInt()));
      failIfSuccessful(() -> queue.removeLastOccurrence(new MutableInt()));
      failIfSuccessful(() -> queue.offer(new MutableInt()));
      failIfSuccessful(queue::descendingIterator);
   }

   private void failIfSuccessful(Runnable runnable)
   {
      try
      {
         runnable.run();
         fail();
      }
      catch(Exception e)
      {
      }
   }
}
