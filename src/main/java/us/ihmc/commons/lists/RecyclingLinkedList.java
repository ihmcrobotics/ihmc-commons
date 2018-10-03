package us.ihmc.commons.lists;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * The {@link RecyclingLinkedList} provides a data structure that can be used similar to a deque. Elements can
 * be added and removed efficiently from the front and the end of the linked list. However, element access is
 * not possible. The linked list provides a forward and backward {@link RecyclingIterator} that can be used to
 * move through the list.
 * <p>
 * If used in a way such that the initial max size is not exceeded this class will be able to operate allocation
 * free by recycling objects that are removed from the list. If the list grows to exceed the number of elements
 * that are allocated at construction time more objects will be allocated.
 * </p>
 * <p>
 * Note, that due to the recycling nature of this class the data stored in the linked list is not available from
 * the outside of this class. Access is provided only through packing of objects. No reference to objects of type
 * {@code T} that are passed to this class is stored and no reference to list-internal objects is available to
 * callers.
 * </p>
 *
 * @author Georg Wiedebach
 *
 * @param <T> the class of the objects stored in this linked list.
 */
public class RecyclingLinkedList<T>
{
   public static final int defaultNumberOfElements = 16;

   private final Supplier<T> typeBuilder;
   private final List<Element> unusedElements = new ArrayList<>();
   private final BiConsumer<T, T> copier;

   private final List<RecyclingLinkedListIterator> iterators = new ArrayList<>();

   private Element first;
   private Element last;
   private int size;

   /**
    * Creates a new {@link RecyclingLinkedList} with the default initial size.
    *
    * @param typeBuilder supplies a new instance of data type {@code T}.
    * @param copier copies such that {@link BiConsumer#accept} sets the first argument from the second
    */
   public RecyclingLinkedList(Supplier<T> typeBuilder, BiConsumer<T, T> copier)
   {
      this(defaultNumberOfElements, typeBuilder, copier);
   }

   /**
    * Creates a new {@link RecyclingLinkedList} with the default initial size.
    *
    * @param objectClass class {@code T}. Must have an empty constructor.
    * @param copier copies such that {@link BiConsumer#accept} sets the first argument from the second
    */
   public RecyclingLinkedList(Class<T> objectClass, BiConsumer<T, T> copier)
   {
      this(defaultNumberOfElements, SupplierBuilder.createFromEmptyConstructor(objectClass), copier);
   }

   /**
    * Creates a new {@link RecyclingLinkedList}.
    *
    * @param numElements is the number of initially allocated elements.
    * @param objectClass class {@code T}. Must have an empty constructor.
    * @param copier copies such that {@link BiConsumer#accept} sets the first argument from the second
    */
   public RecyclingLinkedList(int numElements, Class<T> objectClass, BiConsumer<T, T> copier)
   {
      this(numElements, SupplierBuilder.createFromEmptyConstructor(objectClass), copier);
   }

   /**
    * Creates a new {@link RecyclingLinkedList}.
    *
    * @param numElements is the number of initially allocated elements.
    * @param typeBuilder supplies a new instance of data type {@code T}.
    * @param copier copies such that {@link BiConsumer#accept} sets the first argument from the second
    */
   public RecyclingLinkedList(int numElements, Supplier<T> typeBuilder, BiConsumer<T, T> copier)
   {
      this.typeBuilder = typeBuilder;
      this.copier = copier;

      for (int i = 0; i < numElements; i++)
      {
         unusedElements.add(new Element());
      }
   }

   /**
    * Adds an element to the front of the linked list and sets it to the provided object. Will attempt to
    * recycle unused or previously removed objects and only allocate new objects if this data structure
    * needs to grow.
    *
    * @param object that the new first element of the linked list will be set to.
    */
   public void addFirst(T object)
   {
      disableIterators();

      Element newFirst = get();
      copier.accept(newFirst.element, object);
      newFirst.next = first;
      first = newFirst;

      if (newFirst.next == null)
      {
         last = newFirst;
      }
      else
      {
         first.next.previous = first;
      }

      size++;
   }

   /**
    * Adds an element to the end of the linked list and sets it to the provided object. Will attempt to
    * recycle unused or previously removed objects and only allocate new objects if this data structure
    * needs to grow.
    *
    * @param object that the new last element of the linked list will be set to.
    */
   public void addLast(T object)
   {
      disableIterators();

      Element newLast = get();
      copier.accept(newLast.element, object);
      newLast.previous = last;
      last = newLast;

      if (newLast.previous == null)
      {
         first = newLast;
      }
      else
      {
         last.previous.next = last;
      }

      size++;
   }

   /**
    * Removes the first element of the linked list.
    */
   public void removeFirst()
   {
      removeFirst(null);
   }

   /**
    * Removes the first element of the linked list and pack the provided object to match that removed
    * element.
    *
    * @param objectToPack will be modified to match the removed element.
    */
   public void removeFirst(T objectToPack)
   {
      disableIterators();
      if (objectToPack != null)
      {
         peekFirst(objectToPack);
      }

      Element newFirst = first.next;
      release(first);
      first = newFirst;

      if (first == null)
      {
         last = null;
      }

      size--;
   }

   /**
    * Removes the last element of the linked list.
    */
   public void removeLast()
   {
      removeLast(null);
   }

   /**
    * Removes the last element of the linked list and pack the provided object to match that removed
    * element.
    *
    * @param objectToPack will be modified to match the removed element.
    */
   public void removeLast(T objectToPack)
   {
      disableIterators();
      if (objectToPack != null)
      {
         peekLast(objectToPack);
      }

      Element newLast = last.previous;
      release(last);
      last = newLast;

      if (last == null)
      {
         first = null;
      }

      size--;
   }

   /**
    * Sets the provided object to match the first element in the linked list.
    *
    * @param objectToPack will be modified to match the first element in the linked list.
    */
   public void peekFirst(T objectToPack)
   {
      if (first == null)
      {
         throw new NoSuchElementException();
      }

      copier.accept(objectToPack, first.element);
   }

   /**
    * Sets the provided object to match the last element in the linked list.
    *
    * @param objectToPack will be modified to match the last element in the linked list.
    */
   public void peekLast(T objectToPack)
   {
      if (last == null)
      {
         throw new NoSuchElementException();
      }

      copier.accept(objectToPack, last.element);
   }

   /**
    * Checks whether the linked list contains any elements.
    *
    * @return {@code true} if the data structure is empty.
    */
   public boolean isEmpty()
   {
      return first == null;
   }

   /**
    * Gets the current number of elements in the linked list.
    *
    * @return the size of the data structure.
    */
   public int size()
   {
      return size;
   }

   /**
    * Creates and returns a new {@link RecyclingIterator} for the linked list. The iterator can be
    * used to move through the list in a forward direction starting at the first element. To reuse
    * the iterator call {@link RecyclingIterator#reset()} which will reset the pointer to the start
    * of this linked list.
    *
    * @return a forward iterator for this linked list.
    */
   public RecyclingIterator<T> createForwardIterator()
   {
      RecyclingLinkedListIterator ret = new RecyclingLinkedListIterator(false);
      iterators.add(ret);
      return ret;
   }

   /**
    * Creates and returns a new {@link RecyclingIterator} for the linked list. The iterator can be
    * used to move through the list in a backward direction starting at the last element. To reuse
    * the iterator call {@link RecyclingIterator#reset()} which will reset the pointer to the end
    * of this linked list.
    *
    * @return a backward iterator for this linked list.
    */
   public RecyclingIterator<T> createBackwardIterator()
   {
      RecyclingLinkedListIterator ret = new RecyclingLinkedListIterator(true);
      iterators.add(ret);
      return ret;
   }

   private void disableIterators()
   {
      for (int i = 0; i < iterators.size(); i++)
      {
         iterators.get(i).informOfModification();
      }
   }

   private void release(Element element)
   {
      if (element.previous != null)
      {
         element.previous.next = null;
         element.previous = null;
      }
      if (element.next != null)
      {
         element.next.previous = null;
         element.next = null;
      }
      unusedElements.add(element);
   }

   private Element get()
   {
      if (unusedElements.isEmpty())
      {
         return new Element();
      }
      else
      {
         return unusedElements.remove(unusedElements.size() - 1);
      }
   }

   private class Element
   {
      final T element = typeBuilder.get();
      Element previous = null;
      Element next = null;

      @Override
      public String toString()
      {
         return "Value: " + element.toString() + (previous == null ? " (First)" : "") + (next == null ? " (Last)" : "");
      }
   }

   private class RecyclingLinkedListIterator implements RecyclingIterator<T>
   {
      private Element nextCursor = first;
      private boolean canIterate = true;

      private final boolean reverse;

      public RecyclingLinkedListIterator(boolean reverse)
      {
         this.reverse = reverse;
      }

      void informOfModification()
      {
         canIterate = false;
      }

      @Override
      public void reset()
      {
         nextCursor = reverse ? last : first;
         canIterate = true;
      }

      @Override
      public boolean hasNext()
      {
         if (!canIterate)
         {
            throw new ConcurrentModificationException();
         }

         return nextCursor != null;
      }

      @Override
      public void next(T objectToPack)
      {
         if (!canIterate)
         {
            throw new ConcurrentModificationException();
         }

         if (nextCursor == null)
         {
            throw new NoSuchElementException();
         }

         if (objectToPack != null)
         {
            copier.accept(objectToPack, nextCursor.element);
         }
         nextCursor = reverse ? nextCursor.previous : nextCursor.next;
      }
   }
}
