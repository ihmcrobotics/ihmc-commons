package us.ihmc.commons.time;

import us.ihmc.commons.MathTools;

/**
 * Defines an immutable time interval, defined in seconds.
 */
public interface TimeIntervalReadOnly
{
   /**
    * Returns the start time that defines this interval.
    * @return start time in seconds.
    */
   double getStartTime();

   /**
    * Returns the end time that defines this interval.
    * @return end time in seconds.
    */
   double getEndTime();

   /**
    * Returns the total duration of the interval. That is, the end time minus the start time.
    */
   default double getDuration()
   {
      return getEndTime() - getStartTime();
   }

   /**
    * Checks whether or not this interval is equal to another interval within a certain epsilon.
    */
   default boolean epsilonEquals(TimeIntervalReadOnly other, double epsilon)
   {
      return MathTools.epsilonEquals(getStartTime(), other.getStartTime(), epsilon) && MathTools.epsilonEquals(getEndTime(), other.getEndTime(), epsilon);
   }

   /**
    * Checks whether or not the value {@param time} is contained within this interval. This considers the interval as a bounded set.
    */
   default boolean intervalContains(double time)
   {
      return MathTools.intervalContains(time, getStartTime(), getEndTime());
   }

   /**
    * Checks whether or not the value {@param time} is contained within some epsilon of this interval.
    */
   default boolean epsilonContains(double time, double epsilon)
   {
      return MathTools.intervalContains(time, getStartTime() - epsilon, getEndTime() + epsilon);
   }

   /**
    * Checks whether or not this interval is a valid interval. That is, that the start time is less than the end time.
    */
   default void checkInterval()
   {
      checkInterval(this);
   }

   /**
    * Checks whether or not the interval is a valid interval. That is, that the start time is less than the end time.
    */
   static void checkInterval(TimeIntervalReadOnly timeInterval)
   {
      if (timeInterval.getEndTime() < timeInterval.getStartTime())
         throw new IllegalArgumentException(
               "The end time is not valid! End time " + timeInterval.getEndTime() + " must be greater than start time " + timeInterval.getStartTime());
   }
}
