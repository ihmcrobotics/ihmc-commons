package us.ihmc.commons.robotics.partNames;

import com.google.common.base.CaseFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArmJointNameTest
{
   @Test
   public void testGetCamelCaseNameForStartOfExpression()
   {
      for (ArmJointName value : ArmJointName.values)
      {
         Assertions.assertEquals(value.getCamelCaseNameForStartOfExpression(), CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, value.name()));
      }
   }
}
