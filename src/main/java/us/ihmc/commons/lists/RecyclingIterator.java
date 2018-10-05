package us.ihmc.commons.lists;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A interface providing the functionality to iterate a recycling data structure in an allocation free way.
 * <p>
 * The interface is similar to the {@link Iterator} in terms of functionality. The main difference is that it allows
 * to reset the iterator if it needs to be reused or the underlying data structure being iterated changed. In
 * addition, the {@link #next(Object)} method sets the provided object to match the next object in the iteration.
 * No reference to the object is returned as recycling data structures might reuse objects.
 * </p>
 *
 * @author Georg Wiedebach
 *
 * @param <T> the type of object that is iterated over.
 */
public interface RecyclingIterator<T>
{
   /**
    * Resets the iterator to the start of the iteration. This must be called after the data structure being iterated
    * has changed to avoid a {@link ConcurrentModificationException}. Also use this to iterate a data structure multiple
    * times without creating a new iterator.
    */
   void reset();

   /**
    * @see Iterator#hasNext()
    * @return whether the iterator has more elements.
    * @throws ConcurrentModificationException if the data-structure being iterated has changed since the last call to
    * {@link RecyclingIterator#reset()}.
    */
   boolean hasNext();

   /**
    * @see Iterator#next()
    * @param objectToPack modified to match the next element in the iteration.
    * @throws NoSuchElementException if the iteration has no more elements.
    * @throws ConcurrentModificationException if the data-structure being iterated has changed since the last call to
    * {@link RecyclingIterator#reset()}.
    */
   void next(T objectToPack);

   /**
    * Advances the iterator by one step without packing an element.
    */
   default void next()
   {
      next(null);
   }
}
