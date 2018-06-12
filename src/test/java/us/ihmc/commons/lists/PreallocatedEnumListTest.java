package us.ihmc.commons.lists;

import org.junit.Assert;
import org.junit.Test;
import us.ihmc.commons.lists.PreallocatedEnumList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class PreallocatedEnumListTest
{
   @Test(timeout = 30000)
   public void testConstructor()
   {
      PreallocatedEnumList<TestEnum> list = new PreallocatedEnumList<>(TestEnum.class, TestEnum.values(), 10);
      assertTrue(list.isEmpty());
      assertTrue(list.size() == 0);
      assertTrue(list.getLast() == null);
   }

   @Test(timeout = 30000)
   public void testAddAndGet()
   {
      PreallocatedEnumList<TestEnum> list = new PreallocatedEnumList<>(TestEnum.class, TestEnum.values(), 20);
      ArrayList<TestEnum> expectedList = new ArrayList<>();

      int finalSize = 10;
      for (int i = 0; i < finalSize; i++)
      {
         TestEnum value = TestEnum.values()[i];
         list.add(value);
         expectedList.add(value);
      }

      assertFalse(list.isEmpty());
      Assert.assertTrue(list.size() == finalSize);
      for (int i = 0; i < finalSize; i++)
      {
         Assert.assertTrue(list.get(i) == expectedList.get(i));
      }

      Assert.assertTrue(list.getLast() == expectedList.get(finalSize - 1));

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
      Assert.assertTrue(list.getLast() == null);

      finalSize = 8;
      for (int i = 0; i < finalSize; i++)
      {
         list.add(TestEnum.values()[i]);
         expectedList.add(TestEnum.values()[i]);
      }

      assertFalse(list.isEmpty());
      Assert.assertTrue(list.size() == finalSize);
      for (int i = 0; i < finalSize; i++)
      {
         Assert.assertTrue(list.get(i) == expectedList.get(i));
      }

      Assert.assertTrue(list.getLast() == expectedList.get(finalSize - 1));

      list.clear();
      expectedList.clear();
      Assert.assertTrue(list.getLast() == null);

      finalSize = 20;
      for (int i = 0; i < finalSize; i++)
      {
         list.add(TestEnum.values()[i]);
         expectedList.add(TestEnum.values()[i]);
      }

      assertFalse(list.isEmpty());
      Assert.assertTrue(list.size() == finalSize);
      for (int i = 0; i < finalSize; i++)
      {
         Assert.assertTrue(list.get(i) == expectedList.get(i));
      }

      Assert.assertTrue(list.getLast() == expectedList.get(finalSize - 1));
   }

   @Test(timeout = 30000)
   public void testRemove()
   {
      int currentSize = 10;
      PreallocatedEnumList<TestEnum> list = new PreallocatedEnumList<>(TestEnum.class, TestEnum.values(), currentSize);
      ArrayList<TestEnum> expectedList = new ArrayList<>();
      for (int i = 0; i < currentSize; i++)
      {
         list.add(TestEnum.values()[i]);
         expectedList.add(TestEnum.values()[i]);
      }

      int indexOfRemovedObject = 3;
      list.remove(indexOfRemovedObject);
      expectedList.remove(indexOfRemovedObject);
      currentSize--;
      Assert.assertTrue(list.size() == currentSize);

      for (int i = 0; i < currentSize; i++)
      {
         Assert.assertTrue(list.get(i) == expectedList.get(i));
      }

      indexOfRemovedObject = currentSize - 1;
      list.remove(indexOfRemovedObject);
      expectedList.remove(indexOfRemovedObject);
      currentSize--;
      Assert.assertTrue(list.size() == currentSize);

      for (int i = 0; i < currentSize; i++)
      {
         Assert.assertTrue(list.get(i) == expectedList.get(i));
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
      Random rand = new Random(141456L);
      int currentSize = 10;
      PreallocatedEnumList<TestEnum> list = new PreallocatedEnumList<>(TestEnum.class, TestEnum.values(), currentSize);
      ArrayList<TestEnum> expectedList = new ArrayList<>();
      for (int i = 0; i < currentSize; i++)
      {
         list.add(TestEnum.values()[i]);
         expectedList.add(TestEnum.values()[i]);
      }

      for (int k = 0; k < 20; k++)
      {
         int indexA = rand.nextInt(currentSize);
         int indexB = rand.nextInt(currentSize);
         list.swap(indexA, indexB);
         Collections.swap(expectedList, indexA, indexB);
         Assert.assertTrue(list.size() == currentSize);

         for (int i = 0; i < currentSize; i++)
         {
            Assert.assertTrue(list.get(i) == expectedList.get(i));
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
   public void testSort()
   {
      PreallocatedEnumList<TestEnum> list = new PreallocatedEnumList<>(TestEnum.class, TestEnum.values(), 4);
      list.add(TestEnum.FOXTROT);
      list.add(TestEnum.UNIFORM);
      list.add(TestEnum.CHARLIE);
      list.add(TestEnum.KILO);
      list.sort(Comparator.comparing(Enum::name));
      assertTrue(list.get(0).equals(TestEnum.CHARLIE));
      assertTrue(list.get(1).equals(TestEnum.FOXTROT));
      assertTrue(list.get(2).equals(TestEnum.KILO));
      assertTrue(list.get(3).equals(TestEnum.UNIFORM));
   }

   private enum TestEnum
   {
      ALFA,
      BRAVO,
      CHARLIE,
      DELTA,
      ECHO,
      FOXTROT,
      GOLF,
      HOTEL,
      INDIA,
      JULIETT,
      KILO,
      LIMA,
      MIKE,
      NOVEMBER,
      OSCAR,
      PAPA,
      QUEBEC,
      ROMEO,
      SIERRA,
      TANGO,
      UNIFORM,
      VICTOR,
      WHISKEY,
      XRAY,
      YANKEE,
      ZULU
   }
}
