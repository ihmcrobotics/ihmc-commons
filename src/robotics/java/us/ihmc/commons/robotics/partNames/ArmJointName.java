package us.ihmc.commons.robotics.partNames;

import org.apache.commons.lang3.StringUtils;

public enum ArmJointName
{
   CLAVICLE_ROLL("clavicleRoll"),
   SHOULDER_YAW("shoulderYaw"),
   SHOULDER_ROLL("shoulderRoll"),
   SHOULDER_PITCH("shoulderPitch"),
   ELBOW_PITCH("elbowPitch"),
   WRIST_ROLL("wristRoll"),
   FIRST_WRIST_PITCH("firstWristPitch"),
   SECOND_WRIST_PITCH("secondWristPitch"),
   ELBOW_ROLL("elbowRoll"),
   ELBOW_YAW("elbowYaw"),
   WRIST_YAW("wristYaw"),
   GRIPPER_YAW("gripperYaw");

   public static final ArmJointName[] values = values();

   private final String camelCaseNameForStartOfExpression;

   ArmJointName(String camelCaseNameForStartOfExpression)
   {
      this.camelCaseNameForStartOfExpression = camelCaseNameForStartOfExpression;
   }

   public String getCamelCaseNameForStartOfExpression()
   {
      return camelCaseNameForStartOfExpression;
   }

   public String getCamelCaseNameForMiddleOfExpression()
   {
      return StringUtils.capitalize(getCamelCaseNameForStartOfExpression());
   }

   @Override
   public String toString()
   {
      return getCamelCaseNameForMiddleOfExpression();
   }
}
