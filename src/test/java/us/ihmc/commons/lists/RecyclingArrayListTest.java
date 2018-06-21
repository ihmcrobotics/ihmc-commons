package us.ihmc.commons.lists;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Test;
import us.ihmc.commons.RandomNumbers;
import us.ihmc.commons.lists.RecyclingArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import static org.junit.Assert.*;

public class RecyclingArrayListTest
{
   @Test(timeout = 30000)
   public void testConstructors()
   {
      RecyclingArrayList<Object> list = new RecyclingArrayList<>(Object.class);
      assertTrue(list.isEmpty());
      assertTrue(list.size() == 0);
      assertTrue(list.getLast() == null);

      list = new RecyclingArrayList<>(Object::new);
      assertTrue(list.isEmpty());
      assertTrue(list.size() == 0);
      assertTrue(list.getLast() == null);

      int capacity = 10;
      list = new RecyclingArrayList<>(capacity, Object.class);
      assertTrue(list.isEmpty());
      assertTrue(list.size() == 0);
      assertTrue(list.getLast() == null);

      list = new RecyclingArrayList<>(capacity, Object::new);
      assertTrue(list.isEmpty());
      assertTrue(list.size() == 0);
      assertTrue(list.getLast() == null);
   }

   @Test(timeout = 30000)
   public void testAddAndGet()
   {
      RecyclingArrayList<Object> list = new RecyclingArrayList<>(0, Object::new);
      ArrayList<Object> expectedList = new ArrayList<>();

      int finalSize = 10;
      for (int i = 0; i < finalSize; i++)
      {
         Object lastObject = list.add();
         expectedList.add(lastObject);
      }

      assertFalse(list.isEmpty());
      assertTrue(list.size() == finalSize);
      for (int i = 0; i < finalSize; i++)
      {
         assertTrue(list.get(i) == expectedList.get(i));
      }

      assertTrue(list.getLast() == expectedList.get(finalSize - 1));

      try
      {
         list.get(finalSize);
         fail();
      }
      catch (IndexOutOfBoundsException e)
      {
         // Good
      }

      list.clear();
      expectedList.clear();
      assertTrue(list.getLast() == null);

      finalSize = 8;
      for (int i = 0; i < finalSize; i++)
      {
         Object lastObject = list.add();
         expectedList.add(lastObject);
      }

      assertFalse(list.isEmpty());
      assertTrue(list.size() == finalSize);
      for (int i = 0; i < finalSize; i++)
      {
         assertTrue(list.get(i) == expectedList.get(i));
      }

      assertTrue(list.getLast() == expectedList.get(finalSize - 1));

      list.clear();
      expectedList.clear();
      assertTrue(list.getLast() == null);

      finalSize = 20;
      for (int i = 0; i < finalSize; i++)
      {
         Object lastObject = list.add();
         expectedList.add(lastObject);
      }

      assertFalse(list.isEmpty());
      assertTrue(list.size() == finalSize);
      for (int i = 0; i < finalSize; i++)
      {
         assertTrue(list.get(i) == expectedList.get(i));
      }

      assertTrue(list.getLast() == expectedList.get(finalSize - 1));
   }

   @Test(timeout = 30000)
   public void testGetAndGrowIfNeeded()
   {
      RecyclingArrayList<Object> list = new RecyclingArrayList<>(0, Object::new);

      assertTrue(list.isEmpty());
      assertTrue(list.size() == 0);

      int newSize = 10;
      Object lastObject = list.getAndGrowIfNeeded(newSize - 1);

      assertFalse(list.isEmpty());
      assertTrue(list.size() == newSize);

      for (int i = 0; i < newSize; i++)
      {
         assertTrue(list.get(i) != null);
         assertTrue(list.get(i) instanceof Object);
      }

      assertTrue(list.get(newSize - 1) == lastObject);
      assertTrue(list.getLast() == lastObject);

      int previousSize = newSize;
      newSize = 3;
      lastObject = list.getAndGrowIfNeeded(newSize - 1);

      assertFalse(list.isEmpty());
      assertTrue(list.size() == previousSize);

      for (int i = 0; i < newSize; i++)
      {
         assertTrue(list.get(i) != null);
         assertTrue(list.get(i) instanceof Object);
      }

      assertTrue(list.get(newSize - 1) == lastObject);
      assertTrue(list.getLast() == list.get(previousSize - 1));

      newSize = 13;
      lastObject = list.getAndGrowIfNeeded(newSize - 1);

      assertFalse(list.isEmpty());
      assertTrue(list.size() == newSize);

      for (int i = 0; i < newSize; i++)
      {
         assertTrue(list.get(i) != null);
         assertTrue(list.get(i) instanceof Object);
      }

      assertTrue(list.get(newSize - 1) == lastObject);
      assertTrue(list.getLast() == lastObject);
   }

   @Test(timeout = 30000)
   public void testFastRemove()
   {
      int currentSize = 10;
      RecyclingArrayList<Object> list = new RecyclingArrayList<>(currentSize, Object::new);

      for (int i = 0; i < currentSize; i++)
      {
         list.add();
      }

      assertTrue(list.size() == currentSize);

      ArrayList<Object> savedList = new ArrayList<>();
      for (int i = 0; i < currentSize; i++)
         savedList.add(list.get(i));

      int indexOfRemovedOject = 3;
      list.fastRemove(indexOfRemovedOject);
      currentSize--;
      assertTrue(list.size() == currentSize);

      for (int i = 0; i < currentSize; i++)
      {
         if (i == indexOfRemovedOject)
            assertTrue(list.get(i) == savedList.get(savedList.size() - 1));
         else
            assertTrue(list.get(i) == savedList.get(i));
      }

      try
      {
         list.fastRemove(currentSize);
         fail();
      }
      catch (IndexOutOfBoundsException e)
      {
         // Good
      }
   }

   @Test(timeout = 30000)
   public void testRemove()
   {
      int currentSize = 10;
      RecyclingArrayList<MutableInt> list = new RecyclingArrayList<>(currentSize, MutableInt::new);
      for (int i = 0; i < currentSize; i++)
         list.add().setValue(10 + i);
      assertTrue(list.size() == currentSize);

      ArrayList<MutableInt> expectedList = new ArrayList<>();
      for (int i = 0; i < currentSize; i++)
         expectedList.add(list.get(i));

      int indexOfRemovedOject = 3;
      list.remove(indexOfRemovedOject);
      expectedList.remove(indexOfRemovedOject);
      currentSize--;
      assertTrue(list.size() == currentSize);

      for (int i = 0; i < currentSize; i++)
      {
         assertTrue(list.get(i) == expectedList.get(i));
      }

      indexOfRemovedOject = currentSize - 1;
      list.remove(indexOfRemovedOject);
      expectedList.remove(indexOfRemovedOject);
      currentSize--;
      assertTrue(list.size() == currentSize);

      for (int i = 0; i < currentSize; i++)
      {
         assertTrue(list.get(i) == expectedList.get(i));
      }

      try
      {
         list.remove(currentSize);
         fail();
      }
      catch (IndexOutOfBoundsException e)
      {
         // Good
      }
   }

   @Test(timeout = 30000)
   public void testSwap()
   {
      Random rand = new Random(541964L);
      int currentSize = 10;
      RecyclingArrayList<MutableInt> list = new RecyclingArrayList<>(currentSize, MutableInt::new);
      for (int i = 0; i < currentSize; i++)
         list.add().setValue(10 + i);
      assertTrue(list.size() == currentSize);

      ArrayList<MutableInt> expectedList = new ArrayList<>();
      for (int i = 0; i < currentSize; i++)
         expectedList.add(list.get(i));

      for (int k = 0; k < 20; k++)
      {
         int indexA = RandomNumbers.nextInt(rand, 0, currentSize - 1);
         int indexB = RandomNumbers.nextInt(rand, 0, currentSize - 1);
         list.swap(indexA, indexB);
         Collections.swap(expectedList, indexA, indexB);
         assertTrue(list.size() == currentSize);

         for (int i = 0; i < currentSize; i++)
         {
            assertTrue(list.get(i) == expectedList.get(i));
         }
      }

      try
      {
         list.swap(0, currentSize);
         fail();
      }
      catch (IndexOutOfBoundsException e)
      {
         // Good
      }

      try
      {
         list.swap(currentSize, 0);
         fail();
      }
      catch (IndexOutOfBoundsException e)
      {
         // Good
      }
   }

   @Test(timeout = 30000)
   public void testInsertAtIndex()
   {
      Random rand = new Random(541964L);
      int currentSize = 10;
      RecyclingArrayList<MutableInt> list = new RecyclingArrayList<>(currentSize, MutableInt::new);
      for (int i = 0; i < currentSize; i++)
         list.add().setValue(10 + i);
      assertTrue(list.size() == currentSize);

      ArrayList<MutableInt> expectedList = new ArrayList<>();
      for (int i = 0; i < currentSize; i++)
         expectedList.add(list.get(i));

      for (int k = 0; k < 20; k++)
      {
         int randomIndex = RandomNumbers.nextInt(rand, 0, currentSize);
         if (k == 5)
            randomIndex = currentSize;
         int newRandomValue = RandomNumbers.nextInt(rand, 0, 52161);
         MutableInt newObject = list.insertAtIndex(randomIndex);
         newObject.setValue(newRandomValue);
         expectedList.add(randomIndex, newObject);
         currentSize++;
         assertTrue(list.size() == currentSize);

         for (int i = 0; i < currentSize; i++)
            assertTrue(list.get(i) == expectedList.get(i));
      }

      try
      {
         list.insertAtIndex(currentSize + 1);
         fail();
      }
      catch (IndexOutOfBoundsException e)
      {
         // Good
      }
   }

   @Test(timeout = 30000)
   public void testShuffle()
   {
      Random random = new Random(541964L);
      int currentSize = 100;
      RecyclingArrayList<MutableInt> list = new RecyclingArrayList<>(currentSize, MutableInt::new);

      for (int i = 0; i < currentSize; i++)
      {
         list.add().setValue(10 + i);
      }

      assertTrue(list.size() == currentSize);

      int sumBefore = 0;
      for (int i = 0; i < list.size(); i++)
      {
         MutableInt value = list.get(i);
         sumBefore = sumBefore + value.intValue();
      }
      assertTrue(sumBefore > 0);

      list.shuffle(random);
      assertTrue(list.size() == currentSize);

      int sumAfter = 0;
      for (int i = 0; i < list.size(); i++)
      {
         MutableInt value = list.get(i);
         sumAfter = sumAfter + value.intValue();
      }

      assertEquals(sumBefore, sumAfter);
   }

   @Test(timeout = 30000)
   public void testIteratorHasNext()
   {
      RecyclingArrayList<Object> list = new RecyclingArrayList<>(0, Object::new);
      assertFalse(list.iterator().hasNext());

      int size = 10;
      for (int i = 0; i < size; i++)
      {
         list.add();
      }

      Iterator<Object> iterator = list.iterator();
      for (int i = 0; i < size; i++)
      {
         assertTrue(iterator.hasNext());
         iterator.next();
      }

      assertFalse(iterator.hasNext());
   }

   @Test(timeout = 30000)
   public void testIteratorNext()
   {
      int size = 15;
      RecyclingArrayList<MutableInt> list = new RecyclingArrayList<>(size, MutableInt::new);

      for (int i = 0; i < size; i++)
      {
         list.add().setValue(i);
      }

      Iterator<MutableInt> iterator = list.iterator();
      for (int i = 0; i < size; i++)
      {
         assertTrue(iterator.next().getValue() == i);
      }
   }

   @Test(timeout = 30000)
   public void testIteratorRemove()
   {
      int initialSize = 8;
      RecyclingArrayList<MutableInt> list = new RecyclingArrayList<>(initialSize, MutableInt::new);

      for (int i = 0; i < initialSize; i++)
      {
         list.add().setValue(i);
      }

      Iterator<MutableInt> iterator = list.iterator();

      // can't remove before calling next
      try
      {
         iterator.remove();
         fail();
      }
      catch(IllegalStateException e)
      {
      }

      // test removing first object
      assertTrue(list.size() == initialSize);
      iterator.next();
      iterator.remove();
      assertTrue(list.size() == initialSize - 1);

      // check that correct object was removed
      for (int i = 0; i < initialSize - 1; i++)
      {
         assertTrue(list.get(i).getValue() == i + 1);
      }

      // test removing remaining objects
      for (int i = 0; i < initialSize - 1; i++)
      {
         iterator.next();
         iterator.remove();
         assertTrue(list.size() == initialSize - (i + 2));
      }
   }

   @Test(timeout = 30000)
   public void testIteratorForEachRemaining()
   {
      int initialSize = 10;
      RecyclingArrayList<MutableInt> list = new RecyclingArrayList<>(initialSize, MutableInt::new);

      for (int i = 0; i < initialSize; i++)
      {
         list.add();
      }

      int testIndex = 5;
      Iterator<MutableInt> iterator = list.iterator();
      for (int i = 0; i < testIndex; i++)
      {
         iterator.next();
      }

      iterator.forEachRemaining((value) -> value.setValue(100));

      for (int i = 0; i < initialSize; i++)
      {
         int value = list.get(i).getValue();
         if(i < testIndex)
         {
            assertTrue(value == 0);
         }
         else
         {
            assertTrue(value == 100);
         }
      }
   }

   @Test(timeout = 30000)
   public void testEmptyConstructor()
   {
      // check constructor doesn't throw exception
      new RecyclingArrayList();
   }

   @Test(timeout = 30000)
   public void testSort()
   {
      RecyclingArrayList<MutableInt> list = new RecyclingArrayList<>(10, MutableInt::new);
      for (int i = 0; i < list.size(); i++)
      {
         list.get(i).setValue(i);
      }
      list.shuffle(new Random(239032L));
      list.sort(MutableInt::compareTo);
      for (int i = 0; i < list.size(); i++)
      {
         assertTrue(list.get(i).getValue() == i);
      }
   }
}
