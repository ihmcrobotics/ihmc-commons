package us.ihmc.commons.allocations;

import java.util.Collections;
import java.util.List;

/**
 * A default implementation of {@link AllocationTest} that will record everything.
 */
public class DefaultAllocationTest implements AllocationTest
{
   @Override
   public List<Class<?>> getClassesOfInterest()
   {
      return Collections.emptyList();
   }

   @Override
   public List<Class<?>> getClassesToIgnore()
   {
      return Collections.emptyList();
   }
}
