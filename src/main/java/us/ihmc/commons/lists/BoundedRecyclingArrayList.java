package us.ihmc.commons.lists;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * <p> A list implementation designed to mimic the functionality of ArrayList
 * while reducing allocation. Once an object is allocated in this list,
 * its reference is retained for the lifetime of the list even if the
 * object is nominally removed.<p>
 *
 * <p> This version has a max capacity, meaning a RuntimeException will be
 * thrown if too many elements are added.<p>
 *
 * <p> Objects are added to the list by calling {@link #add()} and operating
 * on the returned object. None of the List api for adding and setting are supported.
 * For example:
 * <ul>
 * <li> {@code RecyclingArrayList<MutableInt> list = new RecyclingArrayList<>(MutableInt.class);}
 * <li> {@code MutableInt i = list.add();}
 * <li> {@code i.setValue(5);}
 * </ul>
 *
 * @param <T>
 */
public class BoundedRecyclingArrayList<T> implements List<T>
{
   /**
    * Minimum non-zero capacity
    */
   private T[] values;
   private int size = 0;
   /** The capacity bound before OutOfMemoryException */
   private final int maxCapacity;
   /** Minimum non-zero capacity */
   private final int minimumPositiveCapacity;
   private final Supplier<T> allocator;

   /**
    * Constructs zero-sized array without an allocator
    */
   @Deprecated
   public BoundedRecyclingArrayList()
   {
      this(0, 0, (Supplier<T>) null);
   }

   /**
    * Constructs a zero-sized array. An allocator is created which calls the given class's empty constructor
    * @param maxCapacity maximum capacity of the list
    * @param clazz class of element data
    * @see SupplierBuilder#createFromEmptyConstructor(Class)
    */
   public BoundedRecyclingArrayList(int maxCapacity, Class<T> clazz)
   {
      this(0, maxCapacity, SupplierBuilder.createFromEmptyConstructor(clazz));
   }

   /**
    * Constructs a zero-sized, zero-capacity array. This array is populated with objects using the allocator.
    * This allocator is also used for any future allocation.
    * @param maxCapacity maximum capacity of the list
    * @param allocator
    */
   public BoundedRecyclingArrayList(int maxCapacity, Supplier<T> allocator)
   {
      this(0, maxCapacity, allocator);
   }

   /**
    * Constructs a zero-sized array and allocates the given capacity. An allocator is created which calls the given class's empty constructor
    * @param initialCapacity initial capacity of the array
    * @param maxCapacity maximum capacity of the list
    * @param clazz class of element data
    * @see SupplierBuilder#createFromEmptyConstructor(Class)
    */
   public BoundedRecyclingArrayList(int initialCapacity, int maxCapacity, Class<T> clazz)
   {
      this(initialCapacity, maxCapacity, SupplierBuilder.createFromEmptyConstructor(clazz));
   }

   /**
    * Constructs a zero-sized array with the given initial capacity. This array is populated with objects using the allocator. This allocator is also
    * used for any future allocation.
    *
    * @param initialCapacity initial capacity of the array
    * @param maxCapacity maximum capacity of the list
    * @param allocator generates elements by calling {@link Supplier#get()}
    */
   @SuppressWarnings("unchecked")
   public BoundedRecyclingArrayList(int initialCapacity, int maxCapacity, Supplier<T> allocator)
   {
      if(initialCapacity < 0)
      {
         throw new IllegalArgumentException("Illegal capacity: " + initialCapacity);
      }

      this.maxCapacity = maxCapacity;
      this.minimumPositiveCapacity = Math.min(8, maxCapacity);
      this.values = (T[]) new Object[initialCapacity];
      this.allocator = allocator;

      fillElementDataIfNeeded();
   }

   /**
    * Randomly shuffles this list using the random-access implementation described here:
    * {@link Collections#shuffle(List, Random)}
    *
    * @param random number generator used to shuffle list
    */
   public void shuffle(Random random)
   {
      for (int i = size; i > 1; i--)
      {
         unsafeSwap(i - 1, random.nextInt(i));
      }
   }

   /**
    * <p> Returns the nominal number of elements in this list. Only indices in
    * the range {@code [0, size() - 1]} are acceptable for index-based operations.
    *
    * <p> Note this generally differs from the number of allocated elements
    * in the underlying array
    *
    * @return size of this list
    */
   @Override
   public int size()
   {
      return size;
   }

   /**
    * Returns the size at which reallocation will occur.
    *
    * @return current internal array capacity
    */
   public int capacity()
   {
      return values.length;
   }

   /**
    * The maximum capacity of the list.
    *
    * @return max list size
    */
   public int getMaxCapacity()
   {
      return maxCapacity;
   }

   /**
    * @return <tt>true</tt> if this list has size 0
    */
   @Override
   public boolean isEmpty()
   {
      return size == 0;
   }

   /**
    * Sets the size of the list to 0, but does not change its capacity. This method is meant
    * to recycle a list without allocating new backing arrays.
    */
   @Override
   public void clear()
   {
      size = 0;
   }

   /**
    * Add a new element at the end of this list.
    *
    * @return the new element.
    */
   public T add()
   {
      return getAndGrowIfNeeded(size);
   }

   /**
    * Inserts a new element at the specified position in this
    * list. Shifts the element currently at that position (if any) and
    * any subsequent elements to the right (adds one to their indices).
    *
    * @param index index at which the new element is to be inserted
    * @return the new inserted element
    * @throws IndexOutOfBoundsException if the index is out of range
    * (<tt>index &lt; 0 || index &gt;= size()</tt>)
    */
   public T insertAtIndex(int index)
   {
      rangeCheckForInsert(index);

      // First add new element at last index
      T ret = add();

      // Then go trough the list by swapping elements two by two to reach the desired index
      for (int i = size - 1; i > index; i--)
         unsafeFastSwap(i, i - 1);

      return ret;
   }

   /**
    * Returns the element at the specified position in this list.
    *
    * @param i index of the element to return
    * @return the element at the specified position in this list
    * @throws IndexOutOfBoundsException if the index is out of range
    * (<tt>index &lt; 0 || index &gt;= size()</tt>)
    */
   @Override
   public T get(int i)
   {
      rangeCheck(i);
      return unsafeGet(i);
   }

   /**
    * Returns the first element of this list.
    * If the list is empty, it returns {@code null}.
    *
    * @return the first element of this list
    */
   public T getFirst()
   {
      if (isEmpty())
         return null;
      else
         return unsafeGet(0);
   }

   /**
    * Returns the last element of this list.
    * If the list is empty, it returns {@code null}.
    *
    * @return the last element of this list
    */
   public T getLast()
   {
      if (isEmpty())
         return null;
      else
         return unsafeGet(size - 1);
   }

   protected T unsafeGet(int i)
   {
      return values[i];
   }

   /**
    * Returns the element at the specified position in this list.
    * The list will grow if the given index is greater or equal to
    * the size this list.
    *
    * @param index index of the element to return
    * @return the element at the specified position in this list
    * @throws IndexOutOfBoundsException if the index is negative (<tt>index &lt; 0</tt>)
    */
   public T getAndGrowIfNeeded(int index)
   {
      positiveIndexCheck(index);
      size = Math.max(size, index + 1);

      if (index >= values.length)
      {
         ensureCapacity(Math.max(minimumPositiveCapacity, size));
      }

      return values[index];
   }

   /**
    * Removes the element at the specified position in this list.
    * This method is faster than {@link BoundedRecyclingArrayList#remove(int)} but the ith element is swapped with the last element changing the ordering of the list.
    *
    * @param index the index of the element to be removed
    */
   public void fastRemove(int index)
   {
      if (index == size - 1)
      {
         size--;
         return;
      }
      rangeCheck(index);
      unsafeFastSwap(index, --size);
   }

   /**
    * Swap two objects of this list.
    *
    * @param i index of the first object to swap
    * @param j index of the second object to swap
    * @throws IndexOutOfBoundsException if either of the indices is out of range
    * (<tt>i &lt; 0 || i &gt;= size() || j &lt; 0 || j &gt;= size()</tt>)
    */
   public void swap(int i, int j)
   {
      rangeCheck(i);
      rangeCheck(j);

      unsafeSwap(i, j);
   }

   protected void unsafeSwap(int i, int j)
   {
      if (i == j)
         return;

      unsafeFastSwap(i, j);
   }

   private void unsafeFastSwap(int i, int j)
   {
      T t = values[i];
      values[i] = values[j];
      values[j] = t;
   }

   /**
    * Removes the element at the specified position in this list.
    * Shifts any subsequent elements to the left (subtracts one from their
    * indices).
    *
    * @param i the index of the element to be removed
    * @return null.
    */
   @Override
   public T remove(int i)
   {
      if (i == size - 1)
      {
         size--;
         return null;
      }
      rangeCheck(i);

      T t = values[i];

      while (i < size - 1)
      {
         values[i] = values[++i];
      }

      // Do not throw away the removed element, put it at the end of the list instead.
      values[size - 1] = t;
      size--;
      return null;
   }

   /**
    * Removes the first occurrence of the specified element from this list,
    * if it is present.  If the list does not contain the element, it is
    * unchanged.
    *
    * @param object element to be removed from this list, if present
    * @return <tt>true</tt> if this list contained the specified element
    */
   @Override
   public boolean remove(Object object)
   {
      int index = indexOf(object);
      if (index == -1)
         return false;
      else
      {
         remove(index);
         return true;
      }
   }

   /**
    * Sorts the array in place using {@link Arrays::sort}
    * @param comparator to determine element ordering
    */
   @Override
   public void sort(Comparator<? super T> comparator)
   {
      if(size() == 0)
         return;
      Arrays.sort(values, 0, size(), comparator);
   }

   protected void ensureCapacity(int minCapacity)
   {
      if (minCapacity <= values.length)
         return;

      if (minCapacity > maxCapacity) // max capacity exceeded
         throw new OutOfMemoryError("Requested capacity (" + minCapacity + ") is greater than max capacity (" + maxCapacity + ")!");

      int previousArraySize = values.length;
      int newArraySize = previousArraySize + (previousArraySize >> 1); // Add 50%
      if (newArraySize - maxCapacity > 0) // if +50% is more than max, set at max
         newArraySize = maxCapacity;
      if (newArraySize - minCapacity < 0) // if requested more than 50%, raise it the rest
         newArraySize = minCapacity;

      values = Arrays.copyOf(values, newArraySize);

      for (int i = previousArraySize; i < newArraySize; i++)
      {
         values[i] = allocator.get();
      }
   }

   private void fillElementDataIfNeeded()
   {
      for (int i = 0; i < values.length; i++)
      {
         if (values[i] == null)
            values[i] = allocator.get();
      }
   }

   /**
    * Checks if the given index is in range.  If not, throws an appropriate
    * runtime exception.  This method does *not* check if the index is
    * negative: It is always used immediately prior to an array access,
    * which throws an ArrayIndexOutOfBoundsException if index is negative.
    */
   protected void rangeCheck(int index)
   {
      if (index >= size)
         throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
      positiveIndexCheck(index);
   }

   protected void rangeCheckForInsert(int index)
   {
      if (index > size)
         throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
      positiveIndexCheck(index);
   }

   protected void positiveIndexCheck(int index)
   {
      if (index < 0)
         throw new IndexOutOfBoundsException("Index cannot be negative: " + index);
   }

   /**
    * Returns <tt>true</tt> if this list contains the specified element.
    *
    * @param object element whose presence in this list is to be tested
    * @return <tt>true</tt> if this list contains the specified element
    */
   @Override
   public boolean contains(Object object)
   {
      return indexOf(object) >= 0;
   }

   /**
    * Returns the index of the first occurrence of the specified element
    * in this list, or -1 if this list does not contain the element.
    */
   @Override
   public int indexOf(Object object)
   {
      if (object != null)
      {
         for (int i = 0; i < size; i++)
         {
            if (object.equals(values[i]))
               return i;
         }
      }
      return -1;
   }

   /**
    * Returns the index of the last occurrence of the specified element
    * in this list, or -1 if this list does not contain the element.
    */
   @Override
   public int lastIndexOf(Object object)
   {
      if (object != null)
      {
         for (int i = size - 1; i >= 0; i--)
         {
            if (object.equals(values[i]))
               return i;
         }
      }
      return -1;
   }

   /** {@inheritDoc} */
   @Override
   public Object[] toArray()
   {
      return Arrays.copyOf(values, size);
   }

   @SuppressWarnings("unchecked")
   @Override
   public <X> X[] toArray(X[] a)
   {
      if (a.length < size)
         // Make a new array of a's runtime type, but my contents:
         return (X[]) Arrays.copyOf(values, size, a.getClass());
      System.arraycopy(values, 0, a, 0, size);
      if (a.length > size)
         a[size] = null;
      return a;
   }

    /** {@inheritDoc} */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!(obj instanceof List))
         return false;
      List<?> other = (List<?>) obj;
      if (size != other.size())
         return false;
      for (int i = 0; i < size(); i++)
      {
         if (!values[i].equals(other.get(i)))
            return false;
      }
      return true;
   }


   @Override
   public String toString()
   {
      if (isEmpty())
         return "Empty list";

      String ret = "";

      for (int i = 0; i < size - 1; i++)
         ret += unsafeGet(i).toString() + "\n";
      ret += unsafeGet(size - 1).toString();

      return ret;
   }

   /** {@inheritDoc} */
   @Override
   public Iterator<T> iterator()
   {
      return new RecyclingArrayListIterator();
   }

   /** Unsupported operation. */
   @Override
   public boolean add(T e)
   {
      throw new UnsupportedOperationException();
   }

   /** Unsupported operation. */
   @Override
   public boolean containsAll(Collection<?> c)
   {
      throw new UnsupportedOperationException();
   }

   /** Unsupported operation. */
   @Override
   public boolean addAll(Collection<? extends T> c)
   {
      throw new UnsupportedOperationException();
   }

   /** Unsupported operation. */
   @Override
   public boolean addAll(int index, Collection<? extends T> c)
   {
      throw new UnsupportedOperationException();
   }

   /** Unsupported operation. */
   @Override
   public boolean removeAll(Collection<?> c)
   {
      throw new UnsupportedOperationException();
   }

   /** Unsupported operation. */
   @Override
   public boolean retainAll(Collection<?> c)
   {
      throw new UnsupportedOperationException();
   }

   /** Unsupported operation. */
   @Override
   public T set(int index, T element)
   {
      throw new UnsupportedOperationException();
   }

   /** Unsupported operation. */
   @Override
   public void add(int index, T element)
   {
      throw new UnsupportedOperationException();
   }

   /** Unsupported operation. */
   @Override
   public ListIterator<T> listIterator()
   {
      throw new UnsupportedOperationException();
   }

   /** Unsupported operation. */
   @Override
   public ListIterator<T> listIterator(int index)
   {
      throw new UnsupportedOperationException();
   }

   /** Unsupported operation. */
   @Override
   public List<T> subList(int fromIndex, int toIndex)
   {
      throw new UnsupportedOperationException();
   }

   private class RecyclingArrayListIterator implements Iterator<T>
   {
      int nextIndexToReturn = 0;
      boolean removeFlag = true;

      @Override
      public boolean hasNext()
      {
         return nextIndexToReturn <= size() - 1;
      }

      @Override
      public T next()
      {
         removeFlag = false;
         return get(nextIndexToReturn++);
      }

      @Override
      public void remove()
      {
         if(removeFlag)
         {
            throw new IllegalStateException();
         }

         BoundedRecyclingArrayList.this.remove(nextIndexToReturn - 1);
         nextIndexToReturn -= 1;
         removeFlag = true;
      }

      @Override
      public void forEachRemaining(Consumer<? super T> action)
      {
         while (nextIndexToReturn < size())
         {
            action.accept((T) values[nextIndexToReturn++]);
         }
      }
   }
}
