package us.ihmc.commons.lists;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class SupplierBuilder
{
   /**
    * Creates a supplier such that calling {@link Supplier#get} returns
    * an instance of the given class type when calling its empty constructor
    *
    * <p> For example, the following is equivalent to calling {@code Thread thread = new Thread();}
    *
    * <p> {@code Supplier<Thread> supplier = SupplierBuilder.createFromEmptyConstructor(Thread.class);}
    * <p> {@code Thread thread = supplier.get();}
    *
    * @param clazz class of supplied object
    * @param <T> type of supplied object
    * @return supplier
    *
    * @exception RuntimeException if the class does not have an accessible empty constructor, or the constructing
    * class has one of the issues described in {@link Constructor#newInstance}
    */
   public static <T> Supplier<T> createFromEmptyConstructor(Class<T> clazz)
   {
      final Constructor<T> emptyConstructor;
      try
      {
         emptyConstructor = clazz.getConstructor();
      }
      catch(NoSuchMethodException e)
      {
         throw new RuntimeException("Could not find a visible empty constructor in the class: " + clazz.getSimpleName());
      }
      
      return () ->
      {
         try
         {
            return emptyConstructor.newInstance();
         }
         catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
         {
            e.printStackTrace();
            throw new RuntimeException(
                  "Something went wrong in the empty constructor implemented in the class: " + emptyConstructor.getDeclaringClass().getSimpleName());
         }
      };
   }

   public static <T> Supplier<T> indexedSupplier(IntFunction<T> indexBasedSupplier)
   {
      return indexedSupplier(indexBasedSupplier, 0);
   }

   /**
    * <br> Returns a Supplier that creates an object as a function of an index, starting at the given index.
    * On the {@code n}-th call to {@link Supplier#get}, the object returned is {@code indexBasedSupplier.apply(startingIndex + (n - 1))}
    *
    * <br> For instance, given the following Supplier:
    *
    * <br> Supplier&lt;String&gt; stringSupplier = indexedSupplier((index) -> "string_" + index, 3);
    *
    * <br> Successively calling {@code stringSupplier.get()} will return: string_3, string_4, string_5, ...
    *
    * @param indexBasedSupplier creates an object as a function of the current index
    * @param startingIndex initial index
    * @param <T>
    * @return index-based supplier
    */
   public static <T> Supplier<T> indexedSupplier(IntFunction<T> indexBasedSupplier, int startingIndex)
   {
      return new Supplier<T>()
      {
         int currentIndex = startingIndex;

         @Override
         public T get()
         {
            return indexBasedSupplier.apply(currentIndex++);
         }
      };
   }
}
