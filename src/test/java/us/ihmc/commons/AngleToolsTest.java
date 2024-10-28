
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AngleToolsTest
{
   @Test
   public void testConstructor()
           throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException

   }

   @Test
   public void testAngleFromZeroToTwoPi()
   {
      assertEquals(0.0, AngleTools.angleFromZeroToTwoPi(0.0, 0.0), 1e-7, "not equal");
      assertEquals(Math.PI / 4.0, AngleTools.angleFromZeroToTwoPi(1.0, 1.0), 1e-7, "not equal");
      assertEquals(7.0 * Math.PI / 4.0, AngleTools.angleFromZeroToTwoPi(1.0, -1.0), 1e-7,"not equal");
   }
}
