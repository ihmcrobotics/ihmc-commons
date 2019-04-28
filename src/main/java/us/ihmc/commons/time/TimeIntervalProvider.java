package us.ihmc.commons.time;

/**
 * Used to define a class as one that occurs over a time interval.
 */
public interface TimeIntervalProvider
{
   /** Returns a time the time interval */
   TimeIntervalBasics getTimeInterval();
}
