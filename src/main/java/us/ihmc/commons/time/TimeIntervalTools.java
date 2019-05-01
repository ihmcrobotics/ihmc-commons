package us.ihmc.commons.time;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unchecked")
public class TimeIntervalTools
{
   /**
    * Defines a comparator that looks at the start times of a time interval.
    */
   public static Comparator<TimeIntervalProvider> startTimeComparator = (TimeIntervalProvider a, TimeIntervalProvider b) -> {
      double startTimeA = a.getTimeInterval().getStartTime();
      double startTimeB = b.getTimeInterval().getStartTime();
      return Double.compare(startTimeA, startTimeB);
   };

   /**
    * Defines a comparator that looks at the end times of a time interval.
    */
   public static Comparator<TimeIntervalProvider> endTimeComparator = (TimeIntervalProvider a, TimeIntervalProvider b) -> {
      double endTimeA = a.getTimeInterval().getEndTime();
      double endTimeB = b.getTimeInterval().getEndTime();
      return Double.compare(endTimeA, endTimeB);
   };

   /**
    * Checks whether or not interval A and interval B overlap any. That is, is the intersection between the two intervals non-empty.
    * @return true if A overlaps with B
    */
   public static boolean doIntervalsOverlap(TimeIntervalReadOnly intervalA, TimeIntervalReadOnly intervalB)
   {
      if (intervalA.intervalContains(intervalB.getStartTime()))
         return true;

      if (intervalA.intervalContains(intervalB.getEndTime()))
         return true;

      if (intervalB.intervalContains(intervalA.getStartTime()))
         return true;

      return intervalB.intervalContains(intervalB.getEndTime());
   }

   /**
    * Performs an in-place sort of {@param timeIntervalProviders} by start time, with the earliest start time coming first.
    */
   public static void sortByStartTime(List<? extends TimeIntervalProvider> timeIntervalProviders)
   {
      sort((List<TimeIntervalProvider>) timeIntervalProviders, startTimeComparator);
   }

   /**
    * Performs an in-place sort of {@param timeIntervalProviders} by start time, with the latest start time coming first.
    */
   public static void sortByReverseStartTime(List<? extends TimeIntervalProvider> timeIntervalProviders)
   {
      sort((List<TimeIntervalProvider>) timeIntervalProviders, startTimeComparator.reversed());
   }

   /**
    * Performs an in-place sort of {@param timeIntervalProviders} by end time, with the earliest end time coming first.
    */
   public static void sortByEndTime(List<? extends TimeIntervalProvider> timeIntervalProviders)
   {
      sort((List<TimeIntervalProvider>) timeIntervalProviders, endTimeComparator);
   }

   /**
    * Performs an in-place sort of {@param timeIntervalProviders} by end time, with the latest end time coming first.
    */
   public static void sortByReverseEndTime(List<? extends TimeIntervalProvider> timeIntervalProviders)
   {
      sort((List<TimeIntervalProvider>) timeIntervalProviders, endTimeComparator.reversed());
   }

   /**
    * Removes all time intervals from {@param timeIntervalProviders} that have a start time less than {@param time}.
    * @param time time in seconds
    */
   public static void removeStartTimesLessThan(double time, List<? extends TimeIntervalProvider> timeIntervalProviders)
   {
      for (int i = 0; i < timeIntervalProviders.size(); i++)
      {
         if (timeIntervalProviders.get(i).getTimeInterval().getStartTime() < time)
         {
            timeIntervalProviders.remove(i);
            i--;
         }
      }
   }

   /**
    * Removes all time intervals from {@param timeIntervalProviders} that have a start time less than or equal to {@param time}.
    * @param time time in seconds
    */
   public static void removeStartTimesLessThanOrEqualTo(double time, List<? extends TimeIntervalProvider> timeIntervalProviders)
   {
      for (int i = 0; i < timeIntervalProviders.size(); i++)
      {
         if (timeIntervalProviders.get(i).getTimeInterval().getStartTime() <= time)
         {
            timeIntervalProviders.remove(i);
            i--;
         }
      }
   }

   /**
    * Removes all time intervals from {@param timeIntervalProviders} that have a start time greater than {@param time}.
    * @param time time in seconds
    */
   public static void removeStartTimesGreaterThan(double time, List<? extends TimeIntervalProvider> timeIntervalProviders)
   {
      for (int i = timeIntervalProviders.size() - 1; i >= 0; i--)
      {
         if (timeIntervalProviders.get(i).getTimeInterval().getStartTime() > time)
         {
            timeIntervalProviders.remove(i);
         }
      }
   }

   /**
    * Removes all time intervals from {@param timeIntervalProviders} that have a start time greater than or equal to {@param time}.
    * @param time time in seconds
    */
   public static void removeStartTimesGreaterThanOrEqualTo(double time, List<? extends TimeIntervalProvider> timeIntervalProviders)
   {
      for (int i = timeIntervalProviders.size() - 1; i >= 0; i--)
      {
         if (timeIntervalProviders.get(i).getTimeInterval().getStartTime() >= time)
         {
            timeIntervalProviders.remove(i);
         }
      }
   }

   /**
    * Removes all time intervals from {@param timeIntervalProviders} that have an end time less than {@param time}.
    * @param time time in seconds
    */
   public static void removeEndTimesLessThan(double time, List<? extends TimeIntervalProvider> timeIntervalProviders)
   {
      for (int i = 0; i < timeIntervalProviders.size(); i++)
      {
         if (timeIntervalProviders.get(i).getTimeInterval().getEndTime() < time)
         {
            timeIntervalProviders.remove(i);
            i--;
         }
      }
   }

   /**
    * Removes all time intervals from {@param timeIntervalProviders} that have an end time less than or equal to {@param time}.
    * @param time time in seconds
    */
   public static void removeEndTimesLessThanOrEqualTo(double time, List<? extends TimeIntervalProvider> timeIntervalProviders)
   {
      for (int i = 0; i < timeIntervalProviders.size(); i++)
      {
         if (timeIntervalProviders.get(i).getTimeInterval().getEndTime() <= time)
         {
            timeIntervalProviders.remove(i);
            i--;
         }
      }
   }

   /**
    * Removes all time intervals from {@param timeIntervalProviders} that have an end time greater than {@param time}.
    * @param time time in seconds
    */
   public static void removeEndTimesGreaterThan(double time, List<? extends TimeIntervalProvider> timeIntervalProviders)
   {
      for (int i = timeIntervalProviders.size() - 1; i >= 0; i--)
      {
         if (timeIntervalProviders.get(i).getTimeInterval().getEndTime() > time)
         {
            timeIntervalProviders.remove(i);
         }
      }
   }

   /**
    * Removes all time intervals from {@param timeIntervalProviders} that have an end time greater than or equal to {@param time}.
    * @param time time in seconds
    */
   public static void removeEndTimesGreaterThanOrEqualTo(double time, List<? extends TimeIntervalProvider> timeIntervalProviders)
   {
      for (int i = timeIntervalProviders.size() - 1; i >= 0; i--)
      {
         if (timeIntervalProviders.get(i).getTimeInterval().getEndTime() >= time)
         {
            timeIntervalProviders.remove(i);
         }
      }
   }

   /**
    * Gets all time intervals from {@param timeIntervalProviders} that have an end time less than {@param time} and returns them as a
    * new list.
    *
    * !! WARNING !! Generates garbage by creating a new list.
    *
    * @param time time in seconds
    * @return list of time intervals that with an end time less than {@param time}.
    */
   public static <T extends TimeIntervalProvider> List<T> getEndTimesLessThan(double time, List<T> timeIntervalProviders)
   {
      List<T> timeIntervalProvidersToReturn = new ArrayList<>();

      for (int i = 0; i < timeIntervalProviders.size(); i++)
      {
         if (timeIntervalProviders.get(i).getTimeInterval().getEndTime() < time)
         {
            timeIntervalProvidersToReturn.add(timeIntervalProviders.get(i));
         }
      }

      return timeIntervalProvidersToReturn;
   }

   /**
    * Gets all time intervals from {@param timeIntervalProviders} that contain {@param time} and returns them as a new list.
    *
    * !! WARNING !! Generates garbage by creating a new list.
    *
    * @param time time in seconds
    * @return list of time intervals that contain time {@param time}.
    */
   public static <T extends TimeIntervalProvider> List<T> getIntervalsContainingTime(double time, List<T> timeIntervalProviders)
   {
      List<T> timeIntervalProvidersToReturn = new ArrayList<>();

      for (int i = 0; i < timeIntervalProviders.size(); i++)
      {
         if (timeIntervalProviders.get(i).getTimeInterval().intervalContains(time))
         {
            timeIntervalProvidersToReturn.add(timeIntervalProviders.get(i));
         }
      }

      return timeIntervalProvidersToReturn;
   }

   private static <T> void sort(List<T> ts, Comparator<T> comparator)
   {
      boolean ordered = false;

      while (!ordered)
      {
         ordered = true;
         for (int i = 0; i < ts.size() - 1; i++)
         {
            T a = ts.get(i);
            T b = ts.get(i + 1);

            if (comparator.compare(a, b) > 0)
            {
               ordered = false;
               swap(ts, i, i + 1);
            }
         }
      }
   }

   private static <T> void swap(List<T> ts, int a, int b)
   {
      T tmp = ts.get(a);
      ts.set(a, ts.get(b));
      ts.set(b, tmp);
   }
}
