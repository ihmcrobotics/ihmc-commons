package us.ihmc.commons.lists;

import static org.junit.Assert.fail;

import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Assert;
import org.junit.Test;

import us.ihmc.commons.MutationTestFacilitator;

public class RecyclingLinkedListTest
{
   @Test
   public void testOperations()
   {
      RecyclingLinkedList<MutableInt> linkedList = new RecyclingLinkedList<>(MutableInt::new, MutableInt::setValue);

      // Make sure the list behaves predictable and can be reused:
      doOperationsAndAsserts(linkedList, true);
      doOperationsAndAsserts(linkedList, false);
      doOperationsAndAsserts(linkedList, true);
      doOperationsAndAsserts(linkedList, false);
   }

   public void doOperationsAndAsserts(RecyclingLinkedList<MutableInt> linkedList, boolean reverse)
   {
      Assert.assertEquals(0, linkedList.size());
      Assert.assertTrue(linkedList.isEmpty());

      MutableInt element = new MutableInt();
      RecyclingIterator<MutableInt> forwardIterator = linkedList.createForwardIterator();
      RecyclingIterator<MutableInt> backwardIterator = linkedList.createBackwardIterator();
      Assert.assertFalse(forwardIterator.hasNext());

      if (reverse)
      {
         // Add elements such that the result is a list from 5 to 9:
         addElementsToBack(linkedList, forwardIterator, backwardIterator);
         Assert.assertEquals(5, linkedList.size());
         // Add elements such that the result is a list from 0 to 9:
         addElementsToFront(linkedList, forwardIterator, backwardIterator);
         Assert.assertEquals(10, linkedList.size());
      }
      else
      {
         // Add elements such that the result is a list from 0 to 9:
         addElementsToFront(linkedList, forwardIterator, backwardIterator);
         Assert.assertEquals(5, linkedList.size());
         // Add elements such that the result is a list from 5 to 9:
         addElementsToBack(linkedList, forwardIterator, backwardIterator);
         Assert.assertEquals(10, linkedList.size());
      }
      Assert.assertFalse(linkedList.isEmpty());

      // Check the first and last elements:
      linkedList.peekFirst(element);
      Assert.assertEquals(0, element.intValue());
      linkedList.peekLast(element);
      Assert.assertEquals(9, element.intValue());

      // Make sure an iterator will see the values 0 to 9 in order:
      int expected = 0;
      forwardIterator.reset();
      while (forwardIterator.hasNext())
      {
         if (expected > 9)
         {
            fail("Iterator should have stopped.");
         }

         forwardIterator.next(element);
         Assert.assertEquals(expected++, element.intValue());
      }
      Assert.assertEquals(expected, 10);
      forwardIterator.reset();

      expected = 9;
      backwardIterator.reset();
      while (backwardIterator.hasNext())
      {
         if (expected < 0)
         {
            fail("Iterator should have stopped.");
         }

         backwardIterator.next(element);
         Assert.assertEquals(expected--, element.intValue());
      }
      Assert.assertEquals(expected, -1);
      backwardIterator.reset();

      if (reverse)
      {
         // Remove some elements such that the list is from 4 to 7:
         removeFromBack(linkedList, element, forwardIterator, backwardIterator);
         // Remove some elements such that the list is from 4 to 9:
         removeFromFront(linkedList, element, forwardIterator, backwardIterator);
      }
      else
      {
         // Remove some elements such that the list is from 4 to 9:
         removeFromFront(linkedList, element, forwardIterator, backwardIterator);
         // Remove some elements such that the list is from 4 to 7:
         removeFromBack(linkedList, element, forwardIterator, backwardIterator);
      }

      // Make sure the iterator will see the values 4 to 7 in order:
      forwardIterator.reset();
      expected = 4;
      while (forwardIterator.hasNext())
      {
         if (expected > 7)
         {
            fail("Iterator should have stopped.");
         }

         forwardIterator.next(element);
         Assert.assertEquals(expected++, element.intValue());
      }
      Assert.assertEquals(expected, 8);

      backwardIterator.reset();
      expected = 7;
      while (backwardIterator.hasNext())
      {
         if (expected < 4)
         {
            fail("Iterator should have stopped.");
         }

         backwardIterator.next(element);
         Assert.assertEquals(expected--, element.intValue());
      }
      Assert.assertEquals(expected, 3);

      // Make sure the iterator fails after it is at the end:
      try
      {
         forwardIterator.next(element);
         fail("Iterator should have thrown a " + NoSuchElementException.class.getSimpleName() + ".");
      }
      catch (NoSuchElementException e)
      {
      }

      try
      {
         backwardIterator.next(element);
         fail("Iterator should have thrown a " + NoSuchElementException.class.getSimpleName() + ".");
      }
      catch (NoSuchElementException e)
      {
      }

      // Add a values to the end up until 20:
      linkedList.peekLast(element);
      int valueToAdd = element.intValue();
      while (++valueToAdd <= 20)
      {
         element.setValue(valueToAdd);
         linkedList.addLast(element);
      }

      // Add a values to the start down until -20:
      linkedList.peekFirst(element);
      valueToAdd = element.intValue();
      while (--valueToAdd >= -20)
      {
         element.setValue(valueToAdd);
         linkedList.addFirst(element);
      }

      // Make sure the iterator will see the values -20 to 20 in order:
      forwardIterator.reset();
      expected = -20;
      while (forwardIterator.hasNext())
      {
         if (expected > 20)
         {
            fail("Iterator should have stopped.");
         }

         forwardIterator.next(element);
         Assert.assertEquals(expected++, element.intValue());
      }
      Assert.assertEquals(expected, 21);

      backwardIterator.reset();
      expected = 20;
      while (backwardIterator.hasNext())
      {
         if (expected < -20)
         {
            fail("Iterator should have stopped.");
         }

         backwardIterator.next(element);
         Assert.assertEquals(expected--, element.intValue());
      }
      Assert.assertEquals(expected, -21);

      // Remove elements until the list is empty:
      if (reverse)
      {
         for (int i = -20; i <= 20; i++)
         {
            if (i % 2 == 0)
            {
               linkedList.removeFirst(element);
               Assert.assertEquals(i, element.intValue());
            }
            else
            {
               linkedList.removeFirst();
            }
         }
      }
      else
      {
         for (int i = 20; i >= -20; i--)
         {
            if (i % 2 == 0)
            {
               linkedList.removeLast(element);
               Assert.assertEquals(i, element.intValue());
            }
            else
            {
               linkedList.removeLast();
            }
         }
      }
      Assert.assertTrue(linkedList.isEmpty());

      // Make sure there is no more elements:
      try
      {
         linkedList.peekFirst(element);
         fail("Iterator should have thrown a " + NoSuchElementException.class.getSimpleName() + ".");
      }
      catch (NoSuchElementException e)
      {
      }
      try
      {
         linkedList.peekLast(element);
         fail("Iterator should have thrown a " + NoSuchElementException.class.getSimpleName() + ".");
      }
      catch (NoSuchElementException e)
      {
      }
      try
      {
         linkedList.removeFirst(element);
         fail("Iterator should have thrown a " + NoSuchElementException.class.getSimpleName() + ".");
      }
      catch (NoSuchElementException e)
      {
      }
      try
      {
         linkedList.removeLast(element);
         fail("Iterator should have thrown a " + NoSuchElementException.class.getSimpleName() + ".");
      }
      catch (NoSuchElementException e)
      {
      }
   }

   private void removeFromBack(RecyclingLinkedList<MutableInt> linkedList, MutableInt element, RecyclingIterator<MutableInt> forwardIterator,
                               RecyclingIterator<MutableInt> backwardIterator)
   {
      linkedList.removeLast(element);
      Assert.assertEquals(9, element.intValue());
      linkedList.removeLast(element);
      Assert.assertEquals(8, element.intValue());

      // Make sure the iterator fails after a modification:
      try
      {
         forwardIterator.hasNext();
         fail("Iterator should have thrown a " + ConcurrentModificationException.class.getSimpleName() + ".");
      }
      catch (ConcurrentModificationException e)
      {
      }
      forwardIterator.reset();

      // Make sure the iterator fails after a modification:
      try
      {
         backwardIterator.hasNext();
         fail("Iterator should have thrown a " + ConcurrentModificationException.class.getSimpleName() + ".");
      }
      catch (ConcurrentModificationException e)
      {
      }
      backwardIterator.reset();
   }

   private void removeFromFront(RecyclingLinkedList<MutableInt> linkedList, MutableInt element, RecyclingIterator<MutableInt> forwardIterator,
                                RecyclingIterator<MutableInt> backwardIterator)
   {
      linkedList.removeFirst(element);
      Assert.assertEquals(0, element.intValue());
      linkedList.removeFirst(element);
      Assert.assertEquals(1, element.intValue());
      linkedList.removeFirst(element);
      Assert.assertEquals(2, element.intValue());
      linkedList.removeFirst(element);
      Assert.assertEquals(3, element.intValue());

      // Make sure the iterator fails after a modification:
      try
      {
         forwardIterator.hasNext();
         fail("Iterator should have thrown a " + ConcurrentModificationException.class.getSimpleName() + ".");
      }
      catch (ConcurrentModificationException e)
      {
      }
      forwardIterator.reset();

      // Make sure the iterator fails after a modification:
      try
      {
         backwardIterator.hasNext();
         fail("Iterator should have thrown a " + ConcurrentModificationException.class.getSimpleName() + ".");
      }
      catch (ConcurrentModificationException e)
      {
      }
      backwardIterator.reset();
   }

   private void addElementsToBack(RecyclingLinkedList<MutableInt> linkedList, RecyclingIterator<MutableInt> forwardIterator,
                                  RecyclingIterator<MutableInt> backwardIterator)
   {
      linkedList.addLast(new MutableInt(5));
      linkedList.addLast(new MutableInt(6));
      linkedList.addLast(new MutableInt(7));
      linkedList.addLast(new MutableInt(8));
      linkedList.addLast(new MutableInt(9));

      // Make sure the iterator fails after a modification:
      try
      {
         forwardIterator.hasNext();
         fail("Iterator should have thrown a " + ConcurrentModificationException.class.getSimpleName() + ".");
      }
      catch (ConcurrentModificationException e)
      {
      }
      forwardIterator.reset();

      // Make sure the iterator fails after a modification:
      try
      {
         backwardIterator.hasNext();
         fail("Iterator should have thrown a " + ConcurrentModificationException.class.getSimpleName() + ".");
      }
      catch (ConcurrentModificationException e)
      {
      }
      backwardIterator.reset();
   }

   private void addElementsToFront(RecyclingLinkedList<MutableInt> linkedList, RecyclingIterator<MutableInt> forwardIterator,
                                   RecyclingIterator<MutableInt> backwardIterator)
   {
      linkedList.addFirst(new MutableInt(4));
      linkedList.addFirst(new MutableInt(3));
      linkedList.addFirst(new MutableInt(2));
      linkedList.addFirst(new MutableInt(1));
      linkedList.addFirst(new MutableInt(0));

      // Make sure the iterator fails after a modification:
      try
      {
         forwardIterator.hasNext();
         fail("Iterator should have thrown a " + ConcurrentModificationException.class.getSimpleName() + ".");
      }
      catch (ConcurrentModificationException e)
      {
      }
      forwardIterator.reset();

      // Make sure the iterator fails after a modification:
      try
      {
         backwardIterator.hasNext();
         fail("Iterator should have thrown a " + ConcurrentModificationException.class.getSimpleName() + ".");
      }
      catch (ConcurrentModificationException e)
      {
      }
      backwardIterator.reset();
   }

   @Test
   public void testConstructors()
   {
      new RecyclingLinkedList<>(MutableInt::new, MutableInt::setValue);
      new RecyclingLinkedList<>(MutableInt.class, MutableInt::setValue);
      new RecyclingLinkedList<>(0, MutableInt::new, MutableInt::setValue);
      new RecyclingLinkedList<>(0, MutableInt.class, MutableInt::setValue);
      new RecyclingLinkedList<>(100, MutableInt::new, MutableInt::setValue);
      new RecyclingLinkedList<>(100, MutableInt.class, MutableInt::setValue);
   }

   public static void main(String[] args)
   {
      MutationTestFacilitator.facilitateMutationTestForClass(RecyclingLinkedList.class, RecyclingLinkedListTest.class);
   }
}
