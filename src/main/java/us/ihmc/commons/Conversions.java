package us.ihmc.commons;

import java.util.concurrent.TimeUnit;

/**
 * Common conversions useful for making use of some libraries easier to read.
 */
public class Conversions
{
   /** Number of bytes (B) in a kibibyte (KiB) */
   public static final int KIBIBYTES_TO_BYTES = 1024;

   /** Number of bytes (B) in a mebibyte (MiB) */
   public static final int MEBIBYTES_TO_BYTES = 1048576;

   /** Number of bytes (B) in a kilobyte (KB) */
   public static final int KILOBYTES_TO_BYTES = 1000;

   /** Number of bytes (B) in a megabyte (MB) */
   public static final int MEGABYTES_TO_BYTES = 1000000;

   /** <p>2π.</p> Implemented as 2.0 * Math.PI */
   public static final double TWO_PI = 2.0 * Math.PI;

   private Conversions()
   {
      // Disallow construction
   }

   /**
    * Convert kibibytes (KiB) to bytes (B).
    *
    * @param kibibytes number of kibibytes
    * @return bytes number of bytes
    */
   public static int kibibytesToBytes(int kibibytes)
   {
      return kibibytes * KIBIBYTES_TO_BYTES;
   }

   /**
    * Convert kilobytes (KB) to bytes (B).
    *
    * @param kilobytes number of kilobytes
    * @return bytes number of bytes
    */
   public static int kilobytesToBytes(int kilobytes)
   {
      return kilobytes * KILOBYTES_TO_BYTES;
   }

   /**
    * Convert mebibytes (MiB) to bytes (B).
    *
    * @param mebibytes number of mebibytes
    * @return bytes number of bytes
    */
   public static int mebibytesToBytes(int mebibytes)
   {
      return mebibytes * MEBIBYTES_TO_BYTES;
   }

   /**
    * Convert megabytes (MB) to bytes (B).
    *
    * @param megabytes number of megabytes
    * @return bytes number of bytes
    */
   public static int megabytesToBytes(int megabytes)
   {
      return megabytes * MEGABYTES_TO_BYTES;
   }

   /**
    * <p>Convert minutes to seconds.</p>
    *
    * <p>NOTE: These methods exist as a floating point alternative to {@link TimeUnit}.</p>
    *
    * @param minutes time in minutes
    * @return Time in seconds.
    */
   public static double minutesToSeconds(double minutes)
   {
      return minutes * 60.0;
   }

   /**
    * <p>Convert seconds to minutes.</p>
    *
    * <p>NOTE: These methods exist as a floating point alternative to {@link TimeUnit}.</p>
    *
    * @param seconds time in seconds
    * @return Time in minutes.
    */
   public static double secondsToMinutes(double seconds)
   {
      return seconds / 60.0;
   }

   /**
    * <p>Convert seconds to milliseconds.</p>
    *
    * <p>NOTE: These methods exist as a floating point alternative to {@link TimeUnit}.</p>
    *
    * @param seconds time in seconds
    * @return Time in milliseconds.
    */
   public static double secondsToMilliseconds(double seconds)
   {
      return seconds * 1e3;
   }

   /**
    * <p>Convert seconds to nanoseconds.</p>
    *
    * <p>NOTE: These methods exist as a floating point alternative to {@link TimeUnit}.</p>
    *
    * @param seconds time in seconds
    * @return Time in nanoseconds.
    */
   public static long secondsToNanoseconds(double seconds)
   {
      return (long) (seconds * 1e9);
   }

   /**
    * <p>Convert milliseconds to minutes.</p>
    *
    * <p>NOTE: These methods exist as a floating point alternative to {@link TimeUnit}.</p>
    *
    * @param milliseconds time in milliseconds
    * @return Time in minutes.
    */
   public static double millisecondsToMinutes(double milliseconds)
   {
      return milliseconds / 6e4;
   }

   /**
    * <p>Convert milliseconds to seconds.</p>
    *
    * <p>NOTE: These methods exist as a floating point alternative to {@link TimeUnit}.</p>
    *
    * @param milliseconds time in milliseconds
    * @return Time in seconds.
    */
   public static double millisecondsToSeconds(double milliseconds)
   {
      return milliseconds / 1e3;
   }

   /**
    * <p>Convert milliseconds to nanoseconds.</p>
    *
    * <p>NOTE: These methods exist as a floating point alternative to {@link TimeUnit}.</p>
    *
    * @param timeInMilliSeconds time in milliseconds
    * @return Time in nanoseconds.
    */
   public static long millisecondsToNanoseconds(double milliseconds)
   {
      return (long) (milliseconds * 1e6);
   }

   /**
    * <p>Convert milliseconds to nanoseconds.</p>
    *
    * <p>NOTE: These methods exist as a floating point alternative to {@link TimeUnit}.</p>
    *
    * @param timeInMilliSeconds time in milliseconds
    * @return Time in nanoseconds.
    */
   public static long millisecondsToNanoseconds(long milliseconds)
   {
      return milliseconds * 1000000L;
   }

   /**
    * <p>Convert microseconds to seconds.</p>
    *
    * <p>NOTE: These methods exist as a floating point alternative to {@link TimeUnit}.</p>
    *
    * @param microseconds time in microseconds
    * @return Time in seconds.
    */
   public static double microsecondsToSeconds(double microseconds)
   {
      return microseconds / 1e6;
   }

   /**
    * <p>Convert microseconds to nanoseconds.</p>
    *
    * <p>NOTE: These methods exist as a floating point alternative to {@link TimeUnit}.</p>
    *
    * @param microseconds time in microseconds
    * @return Time in nanoseconds.
    */
   public static long microsecondsToNanoseconds(double microseconds)
   {
      return (long) (microseconds * 1e3);
   }

   /**
    * <p>Convert microseconds to nanoseconds.</p>
    *
    * <p>NOTE: These methods exist as a floating point alternative to {@link TimeUnit}.</p>
    *
    * @param microseconds time in microseconds
    * @return Time in nanoseconds.
    */
   public static long microsecondsToNanoseconds(long microseconds)
   {
      return microseconds * 1000L;
   }

   /**
    * <p>Convert nanoseconds to seconds.</p>
    *
    * <p>NOTE: These methods exist as a floating point alternative to {@link TimeUnit}.</p>
    *
    * @param nanoseconds time in nanoseconds
    * @return Time in seconds.
    */
   public static double nanosecondsToSeconds(long nanoseconds)
   {
      return nanoseconds / 1e9;
   }

   /**
    * <p>Convert nanoseconds to milliseconds.</p>
    *
    * <p>NOTE: These methods exist as a floating point alternative to {@link TimeUnit}.</p>
    *
    * @param nanoseconds time in nanoseconds
    * @return Time in milliseconds.
    */
   public static long nanosecondsToMilliseconds(long nanoseconds)
   {
      return nanoseconds / 1000000L;
   }

   /**
    * <p>Convert nanoseconds to milliseconds.</p>
    *
    * <p>NOTE: These methods exist as a floating point alternative to {@link TimeUnit}.</p>
    *
    * @param nanoseconds time in nanoseconds
    * @return Time in milliseconds.
    */
   public static double nanosecondsToMilliseconds(double nanoseconds)
   {
      return nanoseconds / 1e6;
   }

   /**
    * <p>Convert nanoseconds to microseconds.</p>
    *
    * <p>NOTE: These methods exist as a floating point alternative to {@link TimeUnit}.</p>
    *
    * @param nanoseconds time in nanoseconds
    * @return Time in microseconds.
    */
   public static long nanosecondsToMicroseconds(long nanoseconds)
   {
      return nanoseconds / 1000L;
   }

   /**
    * <p>Convert nanoseconds to microseconds.</p>
    *
    * <p>NOTE: These methods exist as a floating point alternative to {@link TimeUnit}.</p>
    *
    * @param nanoseconds time in nanoseconds
    * @return Time in microseconds.
    */
   public static double nanosecondsToMicroseconds(double nanoseconds)
   {
      return nanoseconds / 1e3;
   }

   /**
    * <p>Convert angular velocity in radians per second (rad/s) to angular frequency in Hertz (Hz).</p>
    *
    * @param radiansPerSecond angular velocity in rad/s
    * @return Angular frequency in Hz.
    */
   public static double radiansPerSecondToHertz(double radiansPerSecond)
   {
      return radiansPerSecond / TWO_PI;
   }

   /**
    * Converts a period in seconds (s) to a frequency in hertz (Hz).
    *
    * @param period period in seconds (s)
    * @return frequency in Hertz (Hz)
    */
   public static double secondsToHertz(double period)
   {
      return 1.0 / period;
   }

   /**
    * Converts a frequency in hertz (Hz) to a period in seconds (s).
    *
    * @param frequency frequency in Hertz (Hz)
    * @return period in seconds (s)
    */
   public static double hertzToSeconds(double frequency)
   {
      return 1.0 / frequency;
   }

   /**
    * <p>Convert the amplitude of a signal to decibels (dB) for use in Bode magnitude plots.</p>
    *
    * <p>Implmented as 20 * log10(amplitude).</p>
    *
    * @param amplitude of signal
    * @return Magnitude of signal in dB.
    * @see https://en.wikipedia.org/wiki/Bode_plot
    */
   public static double amplitudeToDecibels(double amplitude)
   {
      return 20.0 * Math.log10(amplitude);
   }
}
