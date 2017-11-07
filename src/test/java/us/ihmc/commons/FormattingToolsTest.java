package us.ihmc.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalTime;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import us.ihmc.commons.FormattingTools;
import us.ihmc.commons.PrintTools;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;

public class FormattingToolsTest
{

	@ContinuousIntegrationTest(estimatedDuration = 0.0)
	@Test(timeout = 30000)
   public void testGetFormattedDecimal3D()
   {
      String resultingFormattedString = FormattingTools.getFormattedDecimal3D(1.2345678);
      assertTrue(resultingFormattedString.equals("1.235"));

      resultingFormattedString = FormattingTools.getFormattedDecimal3D(0.1234);
      assertTrue(resultingFormattedString.equals("0.123"));

      resultingFormattedString = FormattingTools.getFormattedDecimal3D(-0.1234);
      assertTrue(resultingFormattedString.equals("-0.123"));

      resultingFormattedString = FormattingTools.getFormattedDecimal3D(0.0234);
      assertTrue(resultingFormattedString.equals("0.023"));

      resultingFormattedString = FormattingTools.getFormattedDecimal3D(-0.0234);
      assertTrue(resultingFormattedString.equals("-0.023"));

      resultingFormattedString = FormattingTools.getFormattedDecimal3D(22.0234);
      assertTrue(resultingFormattedString.equals("22.023"));

      resultingFormattedString = FormattingTools.getFormattedDecimal3D(-22.0234);
      assertTrue(resultingFormattedString.equals("-22.023"));

   }

	@ContinuousIntegrationTest(estimatedDuration = 0.0)
	@Test(timeout = 30000)
   public void testCapitalizeFirstLetter()
   {
      String resultingString = StringUtils.capitalize("capital");
      assertTrue(resultingString.equals("Capital"));
      resultingString = StringUtils.uncapitalize(resultingString);
      assertTrue(resultingString.equals("capital"));

      resultingString = StringUtils.capitalize("robot");
      assertTrue(resultingString.equals("Robot"));
      resultingString = StringUtils.uncapitalize(resultingString);
      assertTrue(resultingString.equals("robot"));

      resultingString = StringUtils.capitalize("Robot");
      assertTrue(resultingString.equals("Robot"));
      resultingString = StringUtils.uncapitalize(resultingString);
      assertTrue(resultingString.equals("robot"));
      resultingString = StringUtils.uncapitalize(resultingString);
      assertTrue(resultingString.equals("robot"));

   }

	@ContinuousIntegrationTest(estimatedDuration = 0.0)
	@Test(timeout = 30000)
   public void testUnderScoredToCamelCase()
   {
      String resultingFormattedString;
      resultingFormattedString = FormattingTools.underscoredToCamelCase("TEST_ABCD_DEFG", true);
      assertTrue(resultingFormattedString.equals("TestAbcdDefg"));

      resultingFormattedString = FormattingTools.underscoredToCamelCase("TEST_ABCD_DEFG", false);
      assertTrue(resultingFormattedString.equals("testAbcdDefg"));

      resultingFormattedString = FormattingTools.underscoredToCamelCase("1234_@$%_BCDF", true);
      assertTrue(resultingFormattedString.equals("1234@$%Bcdf"));
   }
	
	@ContinuousIntegrationTest(estimatedDuration = 0.1)
   @Test(timeout = 3000000)
   public void testFormatToSignificantFigures()
   {
      testFormatToPrecision(123.45, 0.01, 1, "100");
      testFormatToPrecision(123.45, 0.01, 2, "120");
      testFormatToPrecision(0.01000000000001, 0.01, 2, "0.01");
      testFormatToPrecision(-8000000.0000000065, 999999.999999999, 2, "-8000000");
   }

   private void testFormatToPrecision(double value, double precision, int significantFigures, String expectedString)
   {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      PrintStream systemOut = System.out;
      System.setOut(new PrintStream(byteArrayOutputStream));
      System.out.println(FormattingTools.getFormattedToPrecision(value, precision, significantFigures));
      System.out.flush();
      System.setOut(systemOut);
      System.out.println("ByteArrayOutputStream.toString(): " + byteArrayOutputStream.toString());
      assertEquals("FormattingTools.getFormattedToSignificantFigures didn't work.", expectedString + System.lineSeparator(), byteArrayOutputStream.toString());
   }
	
   @ContinuousIntegrationTest(estimatedDuration = 0.0)
   @Test(timeout = 30000)
   public void testGetDateString()
   {
      String dateToolsDateString = FormattingTools.getDateString();
      StringBuilder dateBuilder = new StringBuilder();

      LocalDate now = LocalDate.now();
      int year = now.getYear();
      int month = now.getMonthValue();
      int day = now.getDayOfMonth();

      dateBuilder.append(year);
      if (month / 10 < 1)
         dateBuilder.append("0" + month);
      else
         dateBuilder.append(month);

      if (day / 10 < 1)
         dateBuilder.append("0" + day);
      else
         dateBuilder.append(day);

      assertEquals(dateBuilder.toString(), dateToolsDateString);
      
      
      PrintTools.debug(this, FormattingTools.getDateString());
   }

   @ContinuousIntegrationTest(estimatedDuration = 0.0)
   @Test(timeout = 30000)
   public void testGetTimeString()
   {
      StringBuilder timeBuilder = new StringBuilder();
      
      LocalTime now = LocalTime.now();
      int hours = now.getHour();
      int minutes = now.getMinute();
      int seconds = now.getSecond();
      
      String timeString = FormattingTools.getTimeString();
      String timeSecondsString = FormattingTools.getTimeStringWithSeconds();

      if (hours / 10 < 1)
         timeBuilder.append("0" + hours);
      else
         timeBuilder.append(hours);

      if (minutes / 10 < 1)
         timeBuilder.append("0" + minutes);
      else
         timeBuilder.append(minutes);

      assertEquals(timeBuilder.toString(), timeString);
      PrintTools.debug(this, FormattingTools.getTimeString());
      
      if (seconds / 10 < 1)
         timeBuilder.append("0" + seconds);
      else
         timeBuilder.append(seconds);
      
      assertEquals(timeBuilder.toString(), timeSecondsString);
      
      PrintTools.debug(this, FormattingTools.getTimeStringWithSeconds());
   }
}
