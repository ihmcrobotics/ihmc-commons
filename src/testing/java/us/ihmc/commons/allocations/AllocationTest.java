package us.ihmc.commons.allocations;

import com.google.monitoring.runtime.instrumentation.AllocationRecorder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import us.ihmc.commons.lists.RecyclingArrayList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This interface provides the means to profile the garbage generation of code. This is useful
 * in unit tests to check if the java code inside a controller is real time safe.
 * <p>
 * It does this using the {@link com.google.monitoring.runtime.instrumentation.AllocationRecorder}.
 * </p>
 * <p>
 * For an example of how to use this check {@link AllocationRecordingDemo}.
 * </p>
 * @author Georg
 */
public interface AllocationTest
{
   /**
    * To avoid recording allocations that are of no interest it is possible to specify
    * classes of interest here. (i.e. "whitelist") This avoids recording any garbage generated
    * in other places such as SCS. You should add you controller class here. No classes
    * outside of these and the classes they create and the methods they call will be recorded.
    *
    * @return classes that should be monitored for allocations.
    */
   List<Class<?>> getClassesOfInterest();

   /**
    * To avoid recording allocations that are of no interest it is possible to specify
    * classes that should be ignored here. This avoids recording any garbage generated in
    * places like the {@link ClassLoader} or in places that are simulation specific.
    *
    * @return classes that will be ignored when monitoring for allocations.
    */
   List<Class<?>> getClassesToIgnore();

   /**
    * To avoid recording allocations that are of no interest it is possible to specify
    * methods that should be ignored here. This avoids recording any garbage generated in
    * safe places like the {@link RecyclingArrayList#add} or in places that are
    * simulation specific.
    *
    * @return classes that will be ignored when monitoring for allocations.
    */
   default List<String> getMethodsToIgnore()
   {
      return new ArrayList<>();
   }

   /**
    * Will run the provided runnable and return a list of places where allocations occurred.
    * If the returned list is empty no allocations where detected.
    *
    * @param runnable contains the code to be profiled.
    * @return a list of places where objects were allocated.
    */
   default List<Throwable> runAndCollectAllocations(Runnable runnable)
   {
      checkInstrumentation();

      AllocationSampler sampler = new AllocationSampler();
      getClassesOfInterest().forEach(clazz -> sampler.addClassToWatch(clazz.getName()));
      getClassesToIgnore().forEach(clazz -> sampler.addClassToIgnore(clazz.getName()));
      getMethodsToIgnore().forEach(method -> sampler.addBlacklistMethod(method));

      AllocationRecorder.addSampler(sampler);
      runnable.run();
      sampler.stop();
      AllocationRecorder.removeSampler(sampler);

      return removeDuplicateStackTraces(sampler.getAndClearAllocations());
   }

   /**
    * This methos will check if the {@link AllocationRecorder} has an instrumentation. If this is not the case
    * the JVM was probably not started using the correct javaagent. To fix this start the JVM with the argument<br>
    * {@code -javaagent:[your/path/to/]java-allocation-instrumenter-3.1.0.jar}<br>
    *
    * @throws RuntimeException if no instrumentation exists or check for instrumentation failed.
    */
   static void checkInstrumentation()
   {
      try
      {
         Method method = AllocationRecorder.class.getDeclaredMethod("getInstrumentation");
         method.setAccessible(true);
         if (method.invoke(null) == null)
         {
            throw new RuntimeException(AllocationRecorder.class.getSimpleName() + " has no instrumentation.");
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Helper method to remove duplicate stack traces from a list of throwables.
    *
    * @param throwables list to prune of duplicate stack traces.
    * @return list of throwables with unique stack traces.
    */
   static List<Throwable> removeDuplicateStackTraces(List<Throwable> throwables)
   {
      Map<String, Throwable> map = new HashMap<>();
      throwables.forEach(t -> map.put(ExceptionUtils.getStackTrace(t), t));
      return new ArrayList<>(map.values());
   }
}
