package us.ihmc.commons.lists;

import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * This is an implementation of ArrayDeque that will reuse objects, making it more allocation efficient.
 * When elements are removed from this queue, they are stored in a secondary queue. When adding to this
 * queue, the element to add is first copied and the copy is stored in this queue.
 *
 * <p> In order to copy data elements, a {@code copier} must be provided, which implements {@code BiConsumer}
 * such that {@link BiConsumer#accept} sets the first argument from the second.
 *
 * <p> For example, to make a RecyclingArrayDeque of type {@link MutableInt} the copier would be: <br>
 * {@code copier = (object1, object2) -> object1.setValue(object2); } </p>
 *
 * <p> Functional construction is generally possible too, for example: <br>
 * {@code RecyclingArrayDeque<MutableInt> list = new RecyclingArrayDeque<>(MutableInt::new, MutableInt::setValue);}
 * </p>
 *
 * @param <T> the type of object in this deque
 */
public class RecyclingArrayDeque<T> extends ArrayDeque<T>
{
   private static final long serialVersionUID = 8118722036566615731L;
   private static final int defaultNumberOfElements = 16;

   private final Supplier<T> typeBuilder;
   private final ArrayDeque<T> unusedObjects;
   private final BiConsumer<T, T> copier;

   public RecyclingArrayDeque(Supplier<T> typeBuilder, BiConsumer<T, T> copier)
   {
      this(defaultNumberOfElements, typeBuilder, copier);
   }

   public RecyclingArrayDeque(Class<T> objectClass, BiConsumer<T, T> copier)
   {
      this(defaultNumberOfElements, SupplierBuilder.createFromEmptyConstructor(objectClass), copier);
   }

   public RecyclingArrayDeque(int numElements, Class<T> objectClass, BiConsumer<T, T> copier)
   {
      this(numElements, SupplierBuilder.createFromEmptyConstructor(objectClass), copier);
   }

   /**
    * @param numElements lower bound on initial capacity of the deque
    * @param typeBuilder builds instance of data type
    * @param copier copies such that {@link BiConsumer#accept} sets the first argument from the second
    */
   public RecyclingArrayDeque(int numElements, Supplier<T> typeBuilder, BiConsumer<T, T> copier)
   {
      super(numElements);
      this.typeBuilder = typeBuilder;
      this.copier = copier;
      unusedObjects = new ArrayDeque<>(numElements);
      for (int i = 0; i < numElements; i++)
         unusedObjects.add(typeBuilder.get());
   }

   /** {@inheritDoc} */
   @Override
   public int size()
   {
      return super.size();
   }

   /** {@inheritDoc} */
   @Override
   public boolean isEmpty()
   {
      return super.isEmpty();
   }

   /**
    * Add an object at the front of this deque and return it. Because we are recycling objects the object may have data in it.
    *
    * @return the new empty object.
    */
   public T addFirst()
   {
      T newObject = getOrCreateUnusedObject();
      super.addFirst(newObject);
      return newObject;
   }

   /**
    * Add an object at the end of this deque and return it. Because we are recycling objects the object may have data in it.
    *
    * @return the new empty object.
    */
   public T addLast()
   {
      T newObject = getOrCreateUnusedObject();
      super.addLast(newObject);
      return newObject;
   }

   /** {@inheritDoc} */
   @Override
   public boolean add(T newObject)
   {
      return super.add(copyAndReturnLocalObject(newObject));
   }

   /**
    * The deque will be empty after this call returns.
    * The removed elements are saved in a local buffer for recycling purpose to prevent garbage generation.
    */
   @Override
   public void clear()
   {
      while (!super.isEmpty())
         unusedObjects.add(super.pollFirst());
   }

   /** {@inheritDoc} */
   @Override
   public void addFirst(T newObject)
   {
      super.addFirst(copyAndReturnLocalObject(newObject));
   }

   /** {@inheritDoc} */
   @Override
   public void addLast(T newObject)
   {
      super.addLast(copyAndReturnLocalObject(newObject));
   }

   /** {@inheritDoc} */
   @Override
   public void push(T newObject)
   {
      super.push(copyAndReturnLocalObject(newObject));
   }

   /** {@inheritDoc} */
   @Override
   public boolean offerFirst(T newObject)
   {
      return super.offerFirst(copyAndReturnLocalObject(newObject));
   }

   /** {@inheritDoc} */
   @Override
   public boolean offerLast(T newObject)
   {
      return super.offerLast(copyAndReturnLocalObject(newObject));
   }

   /**
    * Warning: The returned element will be reused and modified by this deque when adding a new element.
    * {@inheritDoc}
    */
   @Override
   public T pollFirst()
   {
      T objectToReturn = super.pollFirst();
      if(objectToReturn != null)
         unusedObjects.add(objectToReturn);
      return objectToReturn;
   }

   /**
    * Warning: The returned element will be reused and modified by this deque when adding a new element.
    * {@inheritDoc}
    */
   @Override
   public T pollLast()
   {
      T objectToReturn = super.pollLast();
      if(objectToReturn != null)
         unusedObjects.add(objectToReturn);
      return objectToReturn;
   }

   /**
    * Warning: The returned element will be reused and modified by this deque when adding a new element.
    * {@inheritDoc}
    */
   @Override
   public T remove()
   {
      T objectToReturn = super.remove();
      unusedObjects.add(objectToReturn);
      return objectToReturn;
   }

   /**
    * Warning: The returned element will be reused and modified by this deque when adding a new element.
    * {@inheritDoc}
    */
   @Override
   public T poll()
   {
      T objectToReturn = super.poll();
      if(objectToReturn != null)
         unusedObjects.add(objectToReturn);
      return objectToReturn;
   }

   /**
    * Warning: The returned element will be reused and modified by this deque when adding a new element.
    * {@inheritDoc}
    */
   @Override
   public T pop()
   {
      T objectToReturn = super.pop();
      unusedObjects.add(objectToReturn);
      return objectToReturn;
   }

   private T copyAndReturnLocalObject(T objectToCopy)
   {
      T localObject = getOrCreateUnusedObject();
      copier.accept(localObject, objectToCopy);
      return localObject;
   }

   private T getOrCreateUnusedObject()
   {
      if (unusedObjects.isEmpty())
         return typeBuilder.get();
      else
         return unusedObjects.poll();
   }

   @Override
   public String toString()
   {
      Iterator<T> iterator = super.iterator();
      if (!iterator.hasNext())
         return "[]";

      StringBuilder sb = new StringBuilder();
      sb.append('[');
      for (; ; )
      {
         T nextObject = iterator.next();
         sb.append(nextObject);
         if (!iterator.hasNext())
            return sb.append(']').toString();
         sb.append(',').append(' ');
      }
   }

   /** Unsupported operation. */
   @Override
   public RecyclingArrayDeque<T> clone()
   {
      throw new UnsupportedOperationException();
   }

   /** Unsupported operation. */
   @Override
   public boolean remove(Object o)
   {
      throw new UnsupportedOperationException();
   }

   /** Unsupported operation. */
   @Override
   public boolean contains(Object o)
   {
      throw new UnsupportedOperationException();
   }

   /** Unsupported operation. */
   @Override
   public Iterator<T> iterator()
   {
      throw new UnsupportedOperationException();
   }

   /** Unsupported operation. */
   @Override
   public Object[] toArray()
   {
      throw new UnsupportedOperationException();
   }

   /** Unsupported operation. */
   @Override
   public <T> T[] toArray(T[] a)
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
   public boolean removeFirstOccurrence(Object o)
   {
      throw new UnsupportedOperationException();
   }

   /** Unsupported operation. */
   @Override
   public boolean removeLastOccurrence(Object o)
   {
      throw new UnsupportedOperationException();
   }

   /** Unsupported operation. */
   @Override
   public boolean offer(T e)
   {
      throw new UnsupportedOperationException();
   }

   /** Unsupported operation. */
   @Override
   public Iterator<T> descendingIterator()
   {
      throw new UnsupportedOperationException();
   }
}
