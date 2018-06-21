package us.ihmc.commons.lists;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.Collection;
import java.util.Objects;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * Preallocated list of objects designed to be allocation-free after construction.
 *
 * <p> This object preallocates the maximum number of instances.
 * No setter is provided, use add(), remove() and get(i) to add, remove or get elements and change them in place.
 *
 * @author Jesper Smith
 *
 * @param <T>
 */
public class PreallocatedList<T> implements List<T>
{
   private final Class<T> clazz;
   private final T[] values;
   private int pos = -1;

   /**
    * Temporarily needed to enable Kryo to serialize DDS messages.
    */
   @Deprecated
   public PreallocatedList()
   {
      clazz = null;
      values = null;
   }

   /**
    * Constructs an list that can be grown up to the given maximum size.
    * <tt>maxSize</tt> elements are pre-allocated using the given allocator.
    * @param clazz class of element data. used to create underlying array
    * @param allocator default element creator
    * @param maxSize maximum size the list can grow
    */
   @SuppressWarnings("unchecked")
   public PreallocatedList(Class<T> clazz, Supplier<T> allocator, int maxSize)
   {
      this.clazz = clazz;
      this.values = (T[]) Array.newInstance(clazz, maxSize);
      for (int i = 0; i < maxSize; i++)
      {
         values[i] = allocator.get();
      }
   }

   /**
    * Returns the elements in this list as array
    *
    * This method allocates a new array
    *
    * @return new array of length size();
    */
   @Override
   public T[] toArray()
   {
      @SuppressWarnings("unchecked")
      T[] array = (T[]) Array.newInstance(clazz, size());
      System.arraycopy(values, 0, array, 0, size());
      return array;
   }

   /** {@inheritDoc} */
   @Override
   @SuppressWarnings("unchecked")
   public <S> S[] toArray(S[] dest)
   {
      int size = size();
      if (dest.length < size)
      {
         return (S[]) Arrays.copyOf(values, size, dest.getClass());
      }
      System.arraycopy(values, 0, dest, 0, size);
      if (dest.length > size)
         dest[size] = null;
      return dest;
   }



   /**
    * Clears the list.
    *
    * This function just resets the size to 0. The underlying data objects are not emptied or removed.
    */

   public void resetQuick()
   {
      pos = -1;
   }

   /**
    * Add a value and return a handle to the object.
    *
    * Do not use for Enum sequences.
    *
    * @return value at the last position. This object can still hold data.
    */
   public T add()
   {
      maxCapacityCheck(pos + 1);
      return values[++pos];
   }

   /**
    * Removes the last element in the list. The underlying data object is not emptied or removed
    */
   public void remove()
   {
      nonEmptyCheck();
      --pos;
   }

   /**
    * Removes the element at the specified position in this list.
    * Shifts any subsequent elements to the left (subtracts one from their
    * indices).
    *
    * @param i the index of the element to be removed
    */
   @Override
   public T remove(int i)
   {
      if (i == pos)
      {
         remove();
         return values[i];
      }

      rangeCheck(i);

      T t = values[i];

      while (i < pos)
      {
         values[i] = values[++i];
      }

      // Do not throw away the removed element, put it at the end of the list instead.
      values[pos] = t;
      --pos;
      return t;
   }

   /** {@inheritDoc} */
   @Override
   public boolean remove(Object o)
   {
      int index = indexOf(o);
      if(index == -1)
      {
         return false;
      }
      else
      {
         remove(index);
         return true;
      }
   }

   /** {@inheritDoc} */
   @Override
   public boolean removeAll(Collection<?> c)
   {
      Objects.requireNonNull(c);
      return filterList(c, true);
   }

   /** {@inheritDoc} */
   @Override
   public boolean retainAll(Collection<?> c)
   {
      Objects.requireNonNull(c);
      return filterList(c, false);
   }

   private boolean filterList(Collection<?> c, boolean removeIfPresent)
   {
      boolean listModified = false;
      for (int i = 0; i < size() ;)
      {
         if(c.contains(values[i]) == removeIfPresent)
         {
            remove(i);
            listModified = true;
         }
         else
         {
            i++;
         }
      }
      return listModified;
   }

   /** {@inheritDoc} */
   @Override
   public int indexOf(Object o)
   {
      for (int i = 0; i < size(); i++)
      {
         if(values[i].equals(o))
            return i;
      }
      return -1;
   }

   /** {@inheritDoc} */
   @Override
   public int lastIndexOf(Object o)
   {
      for (int i = pos; i >= 0; i--)
      {
         if(values[i].equals(o))
            return i;
      }
      return -1;
   }

   /**
    * Swap two objects of this list.
    *
    * @param i index of the first object to swap
    * @param j index of the second object to swap
    * @throws ArrayIndexOutOfBoundsException if either of the indices is out of range
    *            (<tt>i &lt; 0 || i &gt;= size() || j &lt; 0 || j &gt;= size()</tt>)
    */
   public void swap(int i, int j)
   {
      rangeCheck(i);
      rangeCheck(j);

      if (i == j)
      {
         return;
      }

      unsafeSwap(i, j);
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

   private void unsafeSwap(int i, int j)
   {
      T t = values[i];
      values[i] = values[j];
      values[j] = t;
   }

   /**
    * Get the element at position i. To change the element, use get() and
    *
    * @param i Position to get element at
    * @return Element at position i.
    */
   @Override
   public T get(int i)
   {
      rangeCheck(i);
      return values[i];
   }

   /**
    * Returns the first element of this list. If the list is empty, it returns {@code null}.
    *
    * @return the first element of this list.
    */
   public T getFirst()
   {
      if (isEmpty())
      {
         return null;
      }
      else
      {
         return values[0];
      }
   }

   /**
    * Returns the last element of this list. If the list is empty, it returns {@code null}.
    *
    * @return the last element of this list.
    */
   public T getLast()
   {
      if (isEmpty())
      {
         return null;
      }
      else
      {
         return values[pos];
      }
   }

   /**
    * Clears the list
    *
    * This function just resets the size to 0.
    *
    * The underlying data objects are not emptied or removed, however this may change in future
    * releases
    *
    */
   @Override
   public void clear()
   {
      resetQuick();
   }

   /**
    * Returns the number of active elements in this list
    */
   @Override
   public int size()
   {
      return pos + 1;
   }

   /**
    * Returns {@code true} if this list contains no elements.
    *
    * @return {@code true} if this list contains no elements.
    */
   @Override
   public boolean isEmpty()
   {
      return size() == 0;
   }

   /**
    * @return the maximum capacity of this list
    */
   public int capacity()
   {
      return values.length;
   }

   /**
    * @return the remaining space in this sequence (capacity() - size())
    */
   public int remaining()
   {
      return capacity() - size();
   }

   private void nonEmptyCheck()
   {
      if (pos < 0)
      {
         throw new ArrayIndexOutOfBoundsException("List is empty");
      }
   }

   private void rangeCheck(int i)
   {
      if (i < 0 || i > pos)
      {
         throw new ArrayIndexOutOfBoundsException("Position is not valid in the list, size is " + size() + ", requested element is " + i);
      }
   }

   private void maxCapacityCheck(int newSize)
   {
      if (newSize >= this.values.length)
      {
         throw new ArrayIndexOutOfBoundsException("Cannot add element to sequence, max size is violated");
      }
   }

   /**
    * Hashcode computed from the size of the array,
    * and respective hashcodes of the current data.
    *
    * @return hashCode for this list
    * @see Arrays#hashCode(Object[])
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + pos;
      result = prime * result + 1237;
      result = prime * result + Arrays.hashCode(values);
      return result;
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
      if (size() != other.size())
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
      String s = "";
      s += clazz.getSimpleName();
      s += " pos: " + pos;
      s += " [";
      for (int i = 0; i < size(); i++)
      {
         if (i > 0)
            s += ", ";
         s += values[i].toString();
      }
      s += "]";
      return s;
   }

   /** {@inheritDoc} */
   @Override
   public boolean contains(Object o)
   {
      for (int i = 0; i < size(); i++)
      {
         if(values[i].equals(o))
         {
            return true;
         }
      }

      return false;
   }

   /** {@inheritDoc} */
   @Override
   public boolean containsAll(Collection<?> c)
   {
      for (Object o : c)
      {
         if (!contains(o))
            return false;
      }
      return true;
   }

   // Unsupported operations

   /**
    * Always throws an {@code UnsupportedOperationException}.
    * Set elements by calling {@link #get(int)} or {@link #add()}
    * and operating on the returned object
    */
   @Override
   public T set(int index, T element)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Always throws an {@code UnsupportedOperationException}.
    * Add elements by calling {@link #add()} and operating on the returned object
    */
   @Override
   public boolean add(T t)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Always throws an {@code UnsupportedOperationException}.
    * Set elements by calling {@link #get(int)} or {@link #add()}
    * and operating on the returned object
    */
   @Override
   public void add(int index, T element)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Always throws an {@code UnsupportedOperationException}.
    * Add elements by calling {@link #add()} and operating on the returned object
    */
   @Override
   public boolean addAll(Collection<? extends T> c)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Always throws an {@code UnsupportedOperationException}.
    * Add elements by calling {@link #add()} and operating on the returned object
    */
   @Override
   public boolean addAll(int index, Collection<? extends T> c)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Always throws an {@code UnsupportedOperationException}.
    * Set elements by calling {@link #get(int)} and operating on the returned object
    */
   @Override
   public void replaceAll(UnaryOperator<T> operator)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Always throws an {@code UnsupportedOperationException}.
    * Iterate using indices and not an iterator
    */
   @Override
   public Iterator<T> iterator()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Always throws an {@code UnsupportedOperationException}.
    * Iterate using indices and not an iterator
    */
   @Override
   public ListIterator<T> listIterator()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Always throws an {@code UnsupportedOperationException}.
    * Iterate using indices and not an iterator
    */
   @Override
   public ListIterator<T> listIterator(int index)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Always throws an {@code UnsupportedOperationException}.
    */
   @Override
   public List<T> subList(int fromIndex, int toIndex)
   {
      throw new UnsupportedOperationException();
   }
}
