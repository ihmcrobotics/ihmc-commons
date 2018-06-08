package us.ihmc.robotics.lists;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
}
