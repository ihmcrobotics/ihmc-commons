package us.ihmc.commons.time;

import org.junit.jupiter.api.Test;
import us.ihmc.commons.Conversions;
import us.ihmc.commons.Epsilons;
import us.ihmc.commons.MutationTestFacilitator;
import us.ihmc.log.LogTools;

import java.util.Random;
import java.util.function.DoubleSupplier;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings(value = "unused")
public class StopwatchTest
{
   @Test
   public void testConstructor()
   {
      Stopwatch stopwatch = new Stopwatch();

      assertEquals(Double.NaN, stopwatch.averageLap(), "didnt NaN");
      assertEquals(Double.NaN, stopwatch.lapElapsed(), "didnt NaN");
      assertEquals(Double.NaN, stopwatch.totalElapsed(), "didnt NaN");
      assertEquals(Double.NaN, stopwatch.lap(), "didnt NaN");

      stopwatch = new Stopwatch(new FakeTimeProvider());

      assertEquals(Double.NaN, stopwatch.averageLap(), "didnt NaN");
      assertEquals(Double.NaN, stopwatch.lapElapsed(), "didnt NaN");
      assertEquals(Double.NaN, stopwatch.totalElapsed(), "didnt NaN");
      assertEquals(Double.NaN, stopwatch.lap(), "didnt NaN");
   }

   public class FakeTimeProvider implements DoubleSupplier
   {
      public double clock = 0.0;

      public void incrementClock(double amount)
      {
         clock += amount;
      }

      @Override
      public double getAsDouble()
      {
         return clock;
      }
   }

   @Test
   public void testStopwatchWithRealTime() throws InterruptedException
   {
      Stopwatch stopwatch = new Stopwatch();

      double averageLap = stopwatch.averageLap();
      LogTools.debug("Lap: " + stopwatch.lap());
      assertEquals(Double.NaN, averageLap, Epsilons.ONE_HUNDREDTH, "averageLap incorrect");

      assertEquals(stopwatch, stopwatch.start(), "return ref not equal");

      double lapElapsed = stopwatch.lapElapsed();
      double totalElapsed = stopwatch.totalElapsed();
      averageLap = stopwatch.averageLap();
      assertEquals(0.0, lapElapsed, Epsilons.ONE_HUNDREDTH, "lapElapsed incorrect");
      assertEquals(0.0, totalElapsed, Epsilons.ONE_HUNDREDTH, "totalElapsed incorrect");
      assertEquals(Double.NaN, averageLap, Epsilons.ONE_HUNDREDTH, "averageLap incorrect");

      double sleepTime1 = 0.5;
      Thread.sleep((long) Conversions.secondsToMilliseconds(sleepTime1));

      double lap = stopwatch.lap();
      averageLap = stopwatch.averageLap();
      assertEquals(sleepTime1, lap, Epsilons.ONE_HUNDREDTH, "lap incorrect");
      assertEquals(sleepTime1, averageLap, Epsilons.ONE_HUNDREDTH, "averageLap incorrect");

      double sleepTime2 = 1.0;
      Thread.sleep((long) Conversions.secondsToMilliseconds(sleepTime2));

      lap = stopwatch.lap();
      averageLap = stopwatch.averageLap();
      assertEquals(sleepTime2, lap, Epsilons.ONE_HUNDREDTH, "lap incorrect");
      assertEquals((sleepTime1 + sleepTime2) / 2.0, averageLap, Epsilons.ONE_HUNDREDTH, "averageLap incorrect");

      stopwatch.resetLap();
      lapElapsed = stopwatch.lapElapsed();
      assertEquals(0.0, lapElapsed, Epsilons.ONE_HUNDREDTH, "lapElapsed incorrect");

      lap = stopwatch.lap();
      averageLap = stopwatch.averageLap();
      assertEquals(0.0, lap, Epsilons.ONE_HUNDREDTH, "lap incorrect");
      assertEquals((sleepTime1 + sleepTime2) / 3.0, averageLap, Epsilons.ONE_HUNDREDTH, "averageLap incorrect");

      double sleepTime3 = 0.3;
      Thread.sleep((long) Conversions.secondsToMilliseconds(sleepTime3));

      lapElapsed = stopwatch.lapElapsed();
      totalElapsed = stopwatch.totalElapsed();
      assertEquals(sleepTime3, lapElapsed, Epsilons.ONE_HUNDREDTH, "lapElapsed incorrect");
      assertEquals(sleepTime1 + sleepTime2 + sleepTime3, totalElapsed, Epsilons.ONE_HUNDREDTH, "totalElapsed incorrect");

      stopwatch.reset();
      lapElapsed = stopwatch.lapElapsed();
      totalElapsed = stopwatch.totalElapsed();
      averageLap = stopwatch.averageLap();
      assertEquals(0.0, lapElapsed, Epsilons.ONE_HUNDREDTH, "lapElapsed incorrect");
      assertEquals(0.0, totalElapsed, Epsilons.ONE_HUNDREDTH, "totalElapsed incorrect");
      assertEquals(Double.NaN, averageLap, Epsilons.ONE_HUNDREDTH, "averageLap incorrect");

      double sleepTime4 = 0.3;
      Thread.sleep((long) Conversions.secondsToMilliseconds(sleepTime4));

      stopwatch.resetLap();
      lapElapsed = stopwatch.lapElapsed();
      assertEquals(0.0, lapElapsed, Epsilons.ONE_HUNDREDTH, "lapElapsed incorrect");
   }

   @Test
   public void testStopwatch()
   {
      FakeTimeProvider fakeTimeProvider = new FakeTimeProvider();
      Stopwatch stopwatch = new Stopwatch(fakeTimeProvider);

      double averageLap = stopwatch.averageLap();
      LogTools.debug("Lap: " + stopwatch.lap());
      assertEquals(Double.NaN, averageLap, Epsilons.ONE_TEN_BILLIONTH, "averageLap incorrect");

      assertEquals(stopwatch, stopwatch.start(), "return ref not equal");

      double lapElapsed = stopwatch.lapElapsed();
      double totalElapsed = stopwatch.totalElapsed();
      averageLap = stopwatch.averageLap();
      assertEquals(0.0, lapElapsed, Epsilons.ONE_TEN_BILLIONTH, "lapElapsed incorrect");
      assertEquals(0.0, totalElapsed, Epsilons.ONE_TEN_BILLIONTH, "totalElapsed incorrect");
      assertEquals(Double.NaN, averageLap, Epsilons.ONE_TEN_BILLIONTH, "averageLap incorrect");

      double sleepTime1 = 0.5;
      fakeTimeProvider.incrementClock(sleepTime1);

      double lap = stopwatch.lap();
      averageLap = stopwatch.averageLap();
      assertEquals(sleepTime1, lap, Epsilons.ONE_TEN_BILLIONTH, "lap incorrect");
      assertEquals(sleepTime1, averageLap, Epsilons.ONE_TEN_BILLIONTH, "averageLap incorrect");

      double sleepTime2 = 1.0;
      fakeTimeProvider.incrementClock(sleepTime2);

      lap = stopwatch.lap();
      averageLap = stopwatch.averageLap();
      assertEquals(sleepTime2, lap, Epsilons.ONE_TEN_BILLIONTH, "lap incorrect");
      assertEquals((sleepTime1 + sleepTime2) / 2.0, averageLap, Epsilons.ONE_TEN_BILLIONTH, "averageLap incorrect");

      stopwatch.resetLap();
      lapElapsed = stopwatch.lapElapsed();
      assertEquals(0.0, lapElapsed, Epsilons.ONE_TEN_BILLIONTH, "lapElapsed incorrect");

      lap = stopwatch.lap();
      averageLap = stopwatch.averageLap();
      assertEquals(0.0, lap, Epsilons.ONE_TEN_BILLIONTH, "lap incorrect");
      assertEquals((sleepTime1 + sleepTime2) / 3.0, averageLap, Epsilons.ONE_TEN_BILLIONTH, "averageLap incorrect");

      double sleepTime3 = 0.3;
      fakeTimeProvider.incrementClock(sleepTime3);

      lapElapsed = stopwatch.lapElapsed();
      totalElapsed = stopwatch.totalElapsed();
      assertEquals(sleepTime3, lapElapsed, Epsilons.ONE_TEN_BILLIONTH, "lapElapsed incorrect");
      assertEquals(sleepTime1 + sleepTime2 + sleepTime3, totalElapsed, Epsilons.ONE_TEN_BILLIONTH, "totalElapsed incorrect");

      stopwatch.reset();
      lapElapsed = stopwatch.lapElapsed();
      totalElapsed = stopwatch.totalElapsed();
      averageLap = stopwatch.averageLap();
      assertEquals(0.0, lapElapsed, Epsilons.ONE_TEN_BILLIONTH, "lapElapsed incorrect");
      assertEquals(0.0, totalElapsed, Epsilons.ONE_TEN_BILLIONTH, "totalElapsed incorrect");
      assertEquals(Double.NaN, averageLap, Epsilons.ONE_TEN_BILLIONTH, "averageLap incorrect");

      double sleepTime4 = 0.3;
      fakeTimeProvider.incrementClock(sleepTime4);

      stopwatch.resetLap();
      lapElapsed = stopwatch.lapElapsed();
      assertEquals(0.0, lapElapsed, Epsilons.ONE_TEN_BILLIONTH, "lapElapsed incorrect");
   }

   @Test
   public void testSuspendAndResume()
   {
      Random random = new Random(12389L);
      FakeTimeProvider fakeTimeProvider = new FakeTimeProvider();
      Stopwatch stopwatch = new Stopwatch(fakeTimeProvider);

      for (int i = 0; i < 10; i++)
      {
         stopwatch.start();
         double randomDuration1 = sleep(fakeTimeProvider, randomTime(random));
         stopwatch.suspend();
         double randomDuration2 = sleep(fakeTimeProvider, randomTime(random));
         stopwatch.resume();
         double randomDuration3 = sleep(fakeTimeProvider, randomTime(random));
         assertTimeEquals(randomDuration1 + randomDuration3, stopwatch.lapElapsed());
         assertTimeEquals(randomDuration1 + randomDuration3, stopwatch.totalElapsed());
         assertTimeEquals(randomDuration1 + randomDuration3, stopwatch.lap());
      }

      for (int i = 0; i < 10; i++)
      {
         stopwatch.start();
         double randomDuration1 = sleep(fakeTimeProvider, randomTime(random));
         stopwatch.resume();
         double randomDuration2 = sleep(fakeTimeProvider, randomTime(random));
         stopwatch.suspend();
         double randomDuration3 = sleep(fakeTimeProvider, randomTime(random));
         stopwatch.resume();
         double randomDuration4 = sleep(fakeTimeProvider, randomTime(random));
         stopwatch.suspend();
         double randomDuration5 = sleep(fakeTimeProvider, randomTime(random));
         double expectedElapsed = randomDuration1 + randomDuration2 + randomDuration4;
         assertTimeEquals(expectedElapsed, stopwatch.lapElapsed());
         assertTimeEquals(expectedElapsed, stopwatch.totalElapsed());
         assertTimeEquals(expectedElapsed, stopwatch.lap());
      }

      for (int i = 0; i < 10; i++)
      {
         stopwatch.reset();
         double randomDuration1 = sleep(fakeTimeProvider, randomTime(random));
         stopwatch.suspend();
         double randomDuration2 = sleep(fakeTimeProvider, randomTime(random));
         stopwatch.resume();
         double randomDuration3 = sleep(fakeTimeProvider, randomTime(random));
         stopwatch.lap();
         double randomDuration4 = sleep(fakeTimeProvider, randomTime(random));
         assertTimeEquals(randomDuration4, stopwatch.lapElapsed());
         assertTimeEquals(randomDuration1 + randomDuration3 + randomDuration4, stopwatch.totalElapsed());
         assertTimeEquals(randomDuration4, stopwatch.lap());
      }

      for (int i = 0; i < 10; i++)
      {
         stopwatch.reset();
         double randomDuration1 = sleep(fakeTimeProvider, randomTime(random));
         stopwatch.suspend();
         double randomDuration2 = sleep(fakeTimeProvider, randomTime(random));
         stopwatch.resume();
         double randomDuration3 = sleep(fakeTimeProvider, randomTime(random));
         stopwatch.lap();
         double randomDuration4 = sleep(fakeTimeProvider, randomTime(random));
         stopwatch.suspend();
         double randomDuration5 = sleep(fakeTimeProvider, randomTime(random));
         assertTimeEquals(randomDuration4, stopwatch.lapElapsed());
         assertTimeEquals(randomDuration1 + randomDuration3 + randomDuration4, stopwatch.totalElapsed());
         assertTimeEquals(randomDuration4, stopwatch.lap());
      }

      for (int i = 0; i < 10; i++)
      {
         stopwatch.reset();
         double randomDuration1 = sleep(fakeTimeProvider, randomTime(random));
         stopwatch.suspend();
         double randomDuration2 = sleep(fakeTimeProvider, randomTime(random));
         stopwatch.resume();
         double randomDuration3 = sleep(fakeTimeProvider, randomTime(random));
         stopwatch.lap();
         double randomDuration4 = sleep(fakeTimeProvider, randomTime(random));
         stopwatch.suspend();
         double randomDuration5 = sleep(fakeTimeProvider, randomTime(random));
         stopwatch.reset();
         double randomDuration6 = sleep(fakeTimeProvider, randomTime(random));
         assertTimeEquals(randomDuration6, stopwatch.lapElapsed());
         assertTimeEquals(randomDuration6, stopwatch.totalElapsed());
         assertTimeEquals(randomDuration6, stopwatch.lap());
         stopwatch.suspend();
         double randomDuration7 = sleep(fakeTimeProvider, randomTime(random));
         stopwatch.resetLap();
         double randomDuration8 = sleep(fakeTimeProvider, randomTime(random));
         assertTimeEquals(randomDuration8, stopwatch.lapElapsed());
         assertTimeEquals(randomDuration8 + randomDuration6, stopwatch.totalElapsed());
         assertTimeEquals(randomDuration8, stopwatch.lap());
      }

      stopwatch.reset();
      {
         double randomDuration1 = randomTime(random);
         double randomDuration2 = randomTime(random);
         double randomDuration3 = randomTime(random);
         for (int i = 0; i < 10; i++)
         {
            sleep(fakeTimeProvider, randomDuration1);
            stopwatch.suspend();
            sleep(fakeTimeProvider, randomDuration2);
            stopwatch.resume();
            sleep(fakeTimeProvider, randomDuration3);
            stopwatch.resetLap();
            sleep(fakeTimeProvider, randomDuration1);
            stopwatch.suspend();
            sleep(fakeTimeProvider, randomDuration1);
            stopwatch.suspend();
            sleep(fakeTimeProvider, randomDuration2);
            stopwatch.resume();
            stopwatch.resume();
            sleep(fakeTimeProvider, randomDuration3);
            assertTimeEquals(randomDuration1 + randomDuration3, stopwatch.lapElapsed());
            stopwatch.lap();
            assertTimeEquals((randomDuration1 + randomDuration3) * (i + 1), stopwatch.totalElapsed());
         }
      }
   }

   private void assertTimeEquals(double expected, double actual)
   {
      LogTools.info("Expected: " + expected + " (s)  Actual: " + actual + " (s)");
      assertEquals(expected, actual, Epsilons.ONE_TEN_BILLIONTH, "Expected time incorrect");
      LogTools.info("Assertions passed!");
   }

   private double randomTime(Random random)
   {
      return Conversions.millisecondsToSeconds(Math.abs(random.nextInt()) % 100);
   }

   private double sleep(FakeTimeProvider fakeTimeProvider, double duration)
   {
      fakeTimeProvider.incrementClock(duration);
      return duration;
   }

   public static void main(String[] args)
   {
      MutationTestFacilitator.facilitateMutationTestForClass(Stopwatch.class, StopwatchTest.class);
   }
}
