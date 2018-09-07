package us.ihmc.commons;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConversionsTest
{
   @Test(timeout = 30000)
   public void kibibytesToBytes()
   {
      Random rand = new Random();
      for (int i = 0; i < 1000; i++)
      {
         int kibibytes = rand.nextInt();
         assertEquals(Conversions.kibibytesToBytes(kibibytes), kibibytes * 1024, 1e-12);
      }
   }

   @Test(timeout = 30000)
   public void kilobytesToBytes()
   {
      Random rand = new Random();
      for (int i = 0; i < 1000; i++)
      {
         int kilobytes = rand.nextInt();
         assertEquals(Conversions.kilobytesToBytes(kilobytes), kilobytes * 1000, 1e-12);
      }
   }

   @Test(timeout = 30000)
   public void megabytesToBytes()
   {
      Random rand = new Random();
      for (int i = 0; i < 1000; i++)
      {
         int megabytes = rand.nextInt();
         assertEquals(Conversions.megabytesToBytes(megabytes), megabytes * 1000000, 1e-12);
      }
   }

   @Test(timeout = 30000)
   public void mebibytesToBytes()
   {
      Random rand = new Random();
      for (int i = 0; i < 1000; i++)
      {
         int mebibytes = rand.nextInt();
         assertEquals(Conversions.mebibytesToBytes(mebibytes), mebibytes * 1048576, 1e-12);
      }
   }

   @Test(timeout = 30000)
   public void testToSeconds()
   {
      long timestamp = 1500000000;

      assertEquals(1.5, Conversions.nanosecondsToSeconds(timestamp), 1e-22);

      assertEquals(-1.5, Conversions.nanosecondsToSeconds(-timestamp), 1e-22);
   }

   @Test(timeout = 30000)
   public void testToNanoSeconds()
   {
      double time = 1.5;

      assertEquals(1500000000, Conversions.secondsToNanoseconds(time));
      assertEquals(-1500000000, Conversions.secondsToNanoseconds(-time));
   }

   @Test(timeout = 30000)
   public void testMicroSecondsToNanoseconds()
   {
      long mSecs = 2;

      Random random = new Random();

      for (int i = 0; i < 100; i++)
      {
         mSecs = (long) random.nextFloat() * 1000;
         assertEquals(mSecs * 1e3, Conversions.microsecondsToNanoseconds(mSecs), 1e-6);
         assertEquals(-mSecs * 1e3, Conversions.microsecondsToNanoseconds(-mSecs), 1e-6);
      }
   }

   @Test(timeout = 30000)
   public void testSecondsToMilliseconds()
   {
      long secs = 2;

      Random random = new Random();

      for (int i = 0; i < 100; i++)
      {
         secs = (long) random.nextFloat() * 1000;
         assertEquals(secs * 1e3, Conversions.secondsToMilliseconds(secs), 1e-6);
         assertEquals(-secs * 1e3, Conversions.secondsToMilliseconds(-secs), 1e-6);
      }
   }

   @Test(timeout = 30000)
   public void testMillisecondsToSeconds()
   {
      long mSecs = 2;

      Random random = new Random();

      for (int i = 0; i < 100; i++)
      {
         mSecs = (long) random.nextFloat() * 1000;
         assertEquals(mSecs * 1e-3, Conversions.millisecondsToSeconds(mSecs), 1e-6);
         assertEquals(-mSecs * 1e-3, Conversions.millisecondsToSeconds(-mSecs), 1e-6);
      }
   }

   @Test(timeout = 30000)
   public void testMillisecondsToMinutes()
   {
      long mSecs = 2;

      Random random = new Random();

      for (int i = 0; i < 100; i++)
      {
         mSecs = (long) random.nextFloat() * 1000;
         assertEquals((mSecs * 1e-3) / 60.0, Conversions.millisecondsToMinutes(mSecs), 1e-6);
         assertEquals((-mSecs * 1e-3) / 60.0, Conversions.millisecondsToMinutes(-mSecs), 1e-6);
      }
   }

   @Test(timeout = 30000)
   public void testMillisecondsToNanoSeconds()
   {
      int mSecs = 2;

      Random random = new Random();

      for (int i = 0; i < 100; i++)
      {
         mSecs = (int) random.nextFloat() * 1000;
         assertEquals((mSecs * 1e6), Conversions.millisecondsToNanoseconds(mSecs), 1e-6);
         assertEquals((-mSecs * 1e6), Conversions.millisecondsToNanoseconds(-mSecs), 1e-6);
      }
   }

   @Test(timeout = 30000)
   public void testMicroSecondsToSeconds()
   {
      int mSecs = 2;

      Random random = new Random();

      for (int i = 0; i < 100; i++)
      {
         mSecs = (int) random.nextFloat() * 1000;
         assertEquals((mSecs * 1e-6), Conversions.microsecondsToSeconds(mSecs), 1e-6);
         assertEquals((-mSecs * 1e-6), Conversions.microsecondsToSeconds(-mSecs), 1e-6);
      }
   }

   @Test(timeout = 30000)
   public void testMinutesToSeconds()
   {
      int mins = 2;

      Random random = new Random();

      for (int i = 0; i < 100; i++)
      {
         mins = (int) random.nextFloat() * 1000;
         assertEquals((mins * 60), Conversions.minutesToSeconds(mins), 1e-6);
         assertEquals((-mins * 60), Conversions.minutesToSeconds(-mins), 1e-6);
      }
   }

   @Test(timeout = 30000)
   public void testSecondsToMinutes()
   {
      int secs = 2;

      Random random = new Random();

      for (int i = 0; i < 100; i++)
      {
         secs = (int) random.nextFloat() * 1000;
         assertEquals((secs / 60.0), Conversions.secondsToMinutes(secs), 1e-6);
         assertEquals((-secs / 60.0), Conversions.secondsToMinutes(-secs), 1e-6);
      }
   }

   @Test(timeout = 30000)
   public void testNanoSecondsToMilliSeconds()
   {
      int nSecs = 2;

      Random random = new Random();

      for (int i = 0; i < 100; i++)
      {
         nSecs = (int) random.nextFloat() * 1000;
         assertEquals((nSecs * 1e-6), Conversions.nanosecondsToMilliseconds(nSecs), 1e-6);
         assertEquals((-nSecs * 1e-6), Conversions.nanosecondsToMilliseconds(-nSecs), 1e-6);
      }
   }

   @Test(timeout = 30000)
   public void testNanoSecondsToMicroSeconds()
   {
      int nSecs = 2;

      Random random = new Random();

      for (int i = 0; i < 100; i++)
      {
         nSecs = (int) random.nextFloat() * 1000;
         assertEquals((nSecs * 1e-3), Conversions.nanosecondsToMicroseconds(nSecs), 1e-6);
         assertEquals((-nSecs * 1e-3), Conversions.nanosecondsToMicroseconds(-nSecs), 1e-6);
      }
   }

   @Test(timeout = 30000)
   public void testMagnitudeToDecibels()
   {
      double epsilon = 1e-10;
      assertEquals(20.0, Conversions.amplitudeToDecibels(10.0), epsilon);
      assertEquals(40.0, Conversions.amplitudeToDecibels(100.0), epsilon);
      assertEquals(28.691378080683975, Conversions.amplitudeToDecibels(27.2), epsilon);

      double[] magnitudes = new double[] {10.0, 100.0, 27.2};
      double[] decibels = new double[magnitudes.length];
      for (int i = 0; i < magnitudes.length; i++)
      {
         decibels[i] = Conversions.amplitudeToDecibels(magnitudes[i]);
      }

      assertEquals(20.0, decibels[0], epsilon);
      assertEquals(40.0, decibels[1], epsilon);
      assertEquals(28.691378080683975, decibels[2], epsilon);
   }

   @Test(timeout = 30000)
   public void testNaN()
   {
      double magnitude = -1.0;
      double decibels = Conversions.amplitudeToDecibels(magnitude);
      assertTrue(Double.isNaN(decibels));
   }

   @Test(timeout = 30000)
   public void testNegativeInfinity()
   {
      double magnitude = 0.0;
      double decibels = Conversions.amplitudeToDecibels(magnitude);
      assertTrue(Double.isInfinite(decibels));
   }

   @Test(timeout = 30000)
   public void testMinutesSecondsConversions()
   {
      assertEquals("Not equal", 60.0, Conversions.minutesToSeconds(1.0), 1e-7);
      assertEquals("Not equal", 12000.0, Conversions.minutesToSeconds(200.0), 1e-7);
      assertEquals("Not equal", 1.0, Conversions.secondsToMinutes(60.0), 1e-7);
      assertEquals("Not equal", 2.0, Conversions.secondsToMinutes(120.0), 1e-7);
      assertEquals("Not equal", 1000.0, Conversions.secondsToMilliseconds(1.0), 1e-7);
      assertEquals("Not equal", 1.0, Conversions.millisecondsToSeconds(1000.0), 1e-7);
      assertEquals("Not equal", 1.0, Conversions.millisecondsToMinutes(60000.0), 1e-7);
      assertEquals("Not equal", 1000000.0, Conversions.millisecondsToNanoseconds(1.0), 1e-7);
      assertEquals("Not equal", 1000000000, Conversions.millisecondsToNanoseconds(1000), 1e-7);
      assertEquals("Not equal", 1.0, Conversions.microsecondsToSeconds(1000000.0), 1e-7);
      assertEquals("Not equal", 1000, Conversions.microsecondsToNanoseconds(1), 1e-7);
      assertEquals("Not equal", 1000.0, Conversions.microsecondsToNanoseconds(1.0), 1e-7);
      assertEquals("Not equal", 0.000001, Conversions.nanosecondsToMilliseconds(1.0), 1e-7);
      assertEquals("Not equal", 1.0, Conversions.nanosecondsToMilliseconds(1000000), 1e-7);
      assertEquals("Not equal", 1000, Conversions.nanosecondsToMicroseconds(1000000), 1e-7);
      assertEquals("Not equal", 0.001, Conversions.nanosecondsToMicroseconds(1.0), 1e-7);
      assertEquals("Not equal", 1, Conversions.nanosecondsToMicroseconds(1000), 1e-7);
   }

   @Test(timeout = 30000)
   public void testRadiansPerSecondToHz()
   {
      double[] freqInRadPerSecond = new double[] {0.0, Math.PI / 4.0, Math.PI, Math.PI * 2.0, Math.PI * 4.0};
      double[] freqInHz = new double[freqInRadPerSecond.length];
      for (int i = 0; i < freqInRadPerSecond.length; i++)
      {
         freqInHz[i] = Conversions.radiansPerSecondToHertz(freqInRadPerSecond[i]);
      }

      double epsilon = 1e-10;
      assertEquals(0.0, freqInHz[0], epsilon);
      assertEquals(0.125, freqInHz[1], epsilon);
      assertEquals(0.5, freqInHz[2], epsilon);
      assertEquals(1.0, freqInHz[3], epsilon);
      assertEquals(2.0, freqInHz[4], epsilon);

      freqInRadPerSecond = new double[] {-0.0, -Math.PI / 4.0, -Math.PI, -Math.PI * 2.0, -Math.PI * 4.0};
      freqInHz = new double[freqInRadPerSecond.length];
      for (int i = 0; i < freqInRadPerSecond.length; i++)
      {
         freqInHz[i] = Conversions.radiansPerSecondToHertz(freqInRadPerSecond[i]);
      }

      assertEquals(-0.0, freqInHz[0], epsilon);
      assertEquals(-0.125, freqInHz[1], epsilon);
      assertEquals(-0.5, freqInHz[2], epsilon);
      assertEquals(-1.0, freqInHz[3], epsilon);
      assertEquals(-2.0, freqInHz[4], epsilon);
   }

   public static void main(String[] args)
   {
      MutationTestFacilitator.facilitateMutationTestForClass(Conversions.class, ConversionsTest.class);
   }
}
