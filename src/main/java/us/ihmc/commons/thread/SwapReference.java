package us.ihmc.commons.thread;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A swap reference to be used as a simple memory barrier between two threads for two objects.
 * It is intended to minimize blocking time by allowing each thread to operate on data simultaneously.
 * <p>
 * Only one of the two threads should take ownership of calling {@link #swap()}.
 * Is it typical that thread one will write to {@link #getForThreadOne()} and thread two will read from {@link #getForThreadTwo()}.
 * If thread one is calling {@link #swap()}, then thread two should only read or modify the value of {@link #getForThreadTwo()}
 * and is free to do so at any time, but must synchronize over this {@link SwapReference} object.
 * Thread one does not need to synchronize over this object because the {@link #swap()} method is synchronized.
 *
 *
 * where one thread writes from A while another reads from B,
 * then swap and vice versa.
 * <p>
 * This is also sometimes called a double buffer.
 * <p>
 * Typical usage is to operations on one instance, using a synchronized block
 * using this object to synchronize over. The swap method in this class is
 * also synchronized over this object, so it will be atomic. You may access
 * the other instance without a synchronized block freely.
 */
public class SwapReference<T>
{
   private final T a;
   private final T b;
   private T forThreadOne;
   private T forThreadTwo;

   /** The supplier will be immediately called twice to initialize two instances. */
   public SwapReference(Supplier<T> supplier)
   {
      this(supplier.get(), supplier.get());
   }

   /** Accepts two existing references. */
   public SwapReference(T a, T b)
   {
      this.a = a;
      this.b = b;
      forThreadOne = a;
      forThreadTwo = b;
   }

   /** Used in conjunction with {@link #SwapReference(Supplier)} to configure the initial instances.*/
   public void initializeBoth(Consumer<T> consumer)
   {
      consumer.accept(a);
      consumer.accept(b);
   }

   /** @return Object A. This is always the same object. */
   public T getA()
   {
      return a;
   }

   /** @return Object B. This is always the same object. */
   public T getB()
   {
      return b;
   }

   /** @return The object that is currently safe to read and modify from thread one. */
   public T getForThreadOne()
   {
      return forThreadOne;
   }

   /** @return The object that is currently safe to read and modify from thread two. */
   public T getForThreadTwo()
   {
      return forThreadTwo;
   }

   /** @return Whether object A is currently safe to read and modify from thread one. */
   public boolean isThreadOneA()
   {
      return forThreadOne == a;
   }

   /**
    * Perform the swap operation atomically.
    */
   public synchronized void swap()
   {
      T temp = forThreadOne;
      forThreadOne = forThreadTwo;
      forThreadTwo = temp;
   }
}
