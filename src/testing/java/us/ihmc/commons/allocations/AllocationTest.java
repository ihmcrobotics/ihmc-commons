package us.ihmc.commons.allocations;

import com.google.monitoring.runtime.instrumentation.AllocationRecorder;
import us.ihmc.commons.RunnableThatThrows;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.exception.ExceptionHandler;
import us.ihmc.commons.lists.RecyclingArrayList;

import java.util.ArrayList;
import java.util.List;

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
public abstract class AllocationTest extends AllocationProfiler
{
   public AllocationTest()
   {
      checkInstrumentation();

      getClassWhitelist().forEach(className -> addClassToWhitelist(className));
      getClassBlacklist().forEach(className -> addClassToBlacklist(className));
      getMethodBlacklist().forEach(method -> addMethodToBlacklist(method));
      getMethodBlacklist().forEach(method -> addMethodToWhitelist(method));
   }

   /**
    * Limit allocation recording to these classes. Leave empty to allow all.
    *
    * @return class name whitelist
    */
   public List<String> getClassWhitelist()
   {
      return new ArrayList<>();
   }

   /**
    * Ignore allocations from classes matching these names.
    *
    * @return class name blacklist
    */
   public List<String> getClassBlacklist()
   {
      return new ArrayList<>();
   }

   /**
    * Ignore allocations from methods matching these names.
    *
    * @return method name blacklist
    */
   public List<String> getMethodBlacklist()
   {
      return new ArrayList<>();
   }

   /**
    * Limit allocation recording to methods matching these names. Leave empty to allow all.
    *
    * @return method name whitelist
    */
   public List<String> getMethodWhitelist()
   {
      return new ArrayList<>();
   }
}
