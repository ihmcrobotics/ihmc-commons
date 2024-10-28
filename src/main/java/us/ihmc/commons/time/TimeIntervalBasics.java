package us.ihmc.commons.time;

/**
 * Defines a mutable time interval, defined in seconds.
 */
public interface TimeIntervalBasics extends TimeIntervalReadOnly
{
   /**
    * Sets the start time of the interval in seconds.
    * Warning: Does not assert that the interval is "valid".
    */
   void setStartTime(double startTime);

   /**
    * Sets the end time of the interval in seconds.
    * Warning: Does not assert that the interval is "valid".
    */
   void setEndTime(double endTime);

   /**
    * Resets this interval to have start times and end times to NaN.
    */
   default void reset()
   {
      setIntervalUnsafe(Double.NaN, Double.NaN);
   }

   /**
    * Sets the time bounds that define the interval, in seconds. This must be a "proper" time interval, where the end time is greater than the start time.
    */
   default void setInterval(double startTime, double endTime)
   {
      setIntervalUnsafe(startTime, endTime);
      checkInterval();
   }

   default void setIntervalUnsafe(double startTime, double endTime)
   {
      setStartTime(startTime);
      setEndTime(endTime);
   }

   /**
    * Sets this interval from another interval.
    */
   default void set(TimeIntervalReadOnly timeInterval)
   {
      setInterval(timeInterval.getStartTime(), timeInterval.getEndTime());
   }

   /**
    * Shifts the start and end time by {@param shiftTime}.
    * @param shiftTime time to shift, in seconds.
    */
   default TimeIntervalBasics shiftInterval(double shiftTime)
   {
      setIntervalUnsafe(getStartTime() + shiftTime, getEndTime() + shiftTime);
      return this;
   }

}
