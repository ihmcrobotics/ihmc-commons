package us.ihmc.commons.lists;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

public class PreallocatedListTest
{
   @Test
   public void testConstructor()
   {
      PreallocatedList<Object> list = new PreallocatedList<>(Object.class, Object::new, 10);
      assertTrue(list.isEmpty());
      assertTrue(list.size() == 0);
      assertTrue(list.getLast() == null);
   }

   @Test
   public void testAddAndGet()
   {
      PreallocatedList<Object> list = new PreallocatedList<>(Object.class, Object::new, 20);
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

   @Test
   public void testRemove()
   {
      PreallocatedList<MutableInt> list = new PreallocatedList<>(MutableInt.class, MutableInt::new, 10);
      int currentSize = 10;
      while (list.size() < currentSize)
         list.add().setValue(10 + list.size());

      ArrayList<MutableInt> expectedList = new ArrayList<>();
      for (int i = 0; i < currentSize; i++)
         expectedList.add(list.get(i));

      int indexOfRemovedObject = 3;
      list.remove(indexOfRemovedObject);
      expectedList.remove(indexOfRemovedObject);
      currentSize--;
      assertTrue(list.size() == currentSize);

      for (int i = 0; i < currentSize; i++)
      {
         assertTrue(list.get(i) == expectedList.get(i));
      }

      indexOfRemovedObject = currentSize - 1;
      list.remove(indexOfRemovedObject);
      expectedList.remove(indexOfRemovedObject);
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

   @Test
   public void testSwap()
   {
      Random rand = new Random(541964L);
      PreallocatedList<MutableInt> list = new PreallocatedList<>(MutableInt.class, MutableInt::new, 10);
      int currentSize = 10;
      while (list.size() < currentSize)
         list.add().setValue(10 + list.size());

      ArrayList<MutableInt> expectedList = new ArrayList<>();
      for (int i = 0; i < currentSize; i++)
         expectedList.add(list.get(i));

      for (int k = 0; k < 20; k++)
      {
         int indexA = rand.nextInt(currentSize);
         int indexB = rand.nextInt(currentSize);
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

   @Test
   public void testSort()
   {
      PreallocatedList<MutableInt> list = new PreallocatedList<>(MutableInt.class, MutableInt::new, 10);
      list.add().setValue(-3);
      list.add().setValue(20);
      list.add().setValue(-10);
      list.add().setValue(19);
      list.add().setValue(50);
      list.sort(MutableInt::compareTo);
      assertTrue(list.get(0).getValue() == -10);
      assertTrue(list.get(1).getValue() == -3);
      assertTrue(list.get(2).getValue() == 19);
      assertTrue(list.get(3).getValue() == 20);
      assertTrue(list.get(4).getValue() == 50);
   }

   @Test
   public void testRemoveIndex()
   {
      int size = 10;
      PreallocatedList<MutableInt> list = new PreallocatedList<>(MutableInt.class, MutableInt::new, size);
      for (int i = 0; i < size; i++)
      {
         list.add().setValue(i);
      }

      assertTrue(list.remove(8).getValue() == 8);
      assertTrue(list.size() == size - 1);
      assertTrue(list.remove(4).getValue() == 4);
      assertTrue(list.size() == size - 2);
      assertTrue(list.remove(2).getValue() == 2);
      assertTrue(list.size() == size - 3);
      assertTrue(list.remove(size - 4).getValue() == size - 1);
      assertTrue(list.size() == size - 4);
   }

   @Test
   public void testRemoveObject()
   {
      int size = 5;
      PreallocatedList<MutableInt> list = new PreallocatedList<>(MutableInt.class, MutableInt::new, size);
      for (int i = 0; i < size; i++)
      {
         list.add().setValue(i);
      }

      // first element is same as last
      list.get(0).setValue(size - 1);

      // should remove first element
      assertTrue(list.remove(new MutableInt(4)));
      assertTrue(list.size() == size - 1);
      assertTrue(list.remove(new MutableInt(2)));
      assertTrue(list.size() == size - 2);

      assertTrue(list.get(0).getValue() == 1);
      assertTrue(list.get(1).getValue() == 3);
      assertTrue(list.get(2).getValue() == 4);
   }

   @Test
   public void testRemoveAll()
   {
      int size = 5;
      PreallocatedList<MutableInt> list = new PreallocatedList<>(MutableInt.class, MutableInt::new, size);
      for (int i = 0; i < size; i++)
      {
         list.add().setValue(i);
      }

      HashSet<MutableInt> elementsToRemove = new HashSet<>();
      elementsToRemove.add(new MutableInt(1));
      elementsToRemove.add(new MutableInt(3));

      list.removeAll(elementsToRemove);

      assertTrue(list.get(0).getValue() == 0);
      assertTrue(list.get(1).getValue() == 2);
      assertTrue(list.get(2).getValue() == 4);
      assertTrue(list.size() == 3);
   }

   @Test
   public void testRetainAll()
   {
      int size = 5;
      PreallocatedList<MutableInt> list = new PreallocatedList<>(MutableInt.class, MutableInt::new, size);
      for (int i = 0; i < size; i++)
      {
         list.add().setValue(i);
      }

      HashSet<MutableInt> elementsToRemove = new HashSet<>();
      elementsToRemove.add(new MutableInt(1));
      elementsToRemove.add(new MutableInt(3));

      list.retainAll(elementsToRemove);

      assertTrue(list.get(0).getValue() == 1);
      assertTrue(list.get(1).getValue() == 3);
      assertTrue(list.size() == 2);
   }

   @Test
   public void testIndexOf()
   {
      int size = 10;
      PreallocatedList<MutableInt> list = new PreallocatedList<>(MutableInt.class, MutableInt::new, size);
      for (int i = 0; i < size; i++)
      {
         list.add().setValue(i % (size / 2));
      }

      for (int i = 0; i < size / 2; i++)
      {
         assertTrue(list.indexOf(new MutableInt(i)) == i);
      }

      for (int i = 0; i < size / 2; i++)
      {
         assertTrue(list.lastIndexOf(new MutableInt(i)) == i + size / 2);
      }

      assertTrue(list.indexOf(new MutableInt(398)) == -1);
      assertTrue(list.lastIndexOf(new MutableInt(398)) == -1);
   }

   @Test
   public void testContains()
   {
      int size = 5;
      PreallocatedList<MutableInt> list = new PreallocatedList<>(MutableInt.class, MutableInt::new, size);
      for (int i = 0; i < size; i++)
      {
         list.add().setValue(i);
      }

      for (int i = 0; i < list.size(); i++)
      {
         assertTrue(list.contains(new MutableInt(i)));
      }
      assertFalse(list.contains(new MutableInt(size)));
      assertFalse(list.contains(new MutableInt(-1)));
      assertFalse(list.contains(new MutableInt(0xdeadbeef)));
   }

   @Test
   public void testContainsAll()
   {
      int size = 5;
      PreallocatedList<MutableInt> list = new PreallocatedList<>(MutableInt.class, MutableInt::new, size);
      HashSet<MutableInt> elements = new HashSet<>();

      for (int i = 0; i < size; i++)
      {
         list.add().setValue(i);
         elements.add(new MutableInt(i));
      }

      assertTrue(list.containsAll(elements));

      elements.add(new MutableInt(size));
      assertFalse(list.containsAll(elements));

      elements.remove(new MutableInt(size));
      elements.remove(new MutableInt(1));
      assertTrue(list.containsAll(elements));
   }

   @Test
   public void testUnsupportedOperations()
   {
      int size = 5;
      PreallocatedList<MutableInt> list = new PreallocatedList<>(MutableInt.class, MutableInt::new, size);
      for (int i = 0; i < size; i++)
      {
         list.add().setValue(i);
      }

      try
      {
         list.set(0, new MutableInt());
         fail();
      }
      catch(UnsupportedOperationException e)
      {
      }

      try
      {
         list.add(new MutableInt());
         fail();
      }
      catch(UnsupportedOperationException e)
      {
      }

      try
      {
         list.add(0, new MutableInt());
         fail();
      }
      catch(UnsupportedOperationException e)
      {
      }

      try
      {
         list.addAll(new HashSet<>());
         fail();
      }
      catch(UnsupportedOperationException e)
      {
      }

      try
      {
         list.replaceAll(UnaryOperator.identity());
         fail();
      }
      catch(UnsupportedOperationException e)
      {
      }

      try
      {
         list.iterator();
         fail();
      }
      catch(UnsupportedOperationException e)
      {
      }

      try
      {
         list.listIterator();
         fail();
      }
      catch(UnsupportedOperationException e)
      {
      }

      try
      {
         list.listIterator(0);
         fail();
      }
      catch(UnsupportedOperationException e)
      {
      }

      try
      {
         list.subList(0, 1);
         fail();
      }
      catch(UnsupportedOperationException e)
      {
      }
   }
}
