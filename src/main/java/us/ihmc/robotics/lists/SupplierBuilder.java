package us.ihmc.robotics.lists;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

public class SupplierBuilder
{
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
