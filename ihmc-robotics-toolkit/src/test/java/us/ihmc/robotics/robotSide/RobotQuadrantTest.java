package us.ihmc.robotics.robotSide;

import static us.ihmc.robotics.Assert.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
public class RobotQuadrantTest
{
   private RobotQuadrant frontLeft = RobotQuadrant.FRONT_LEFT;
   private RobotQuadrant frontRight = RobotQuadrant.FRONT_RIGHT;
   private RobotQuadrant hindRight = RobotQuadrant.HIND_RIGHT;
   private RobotQuadrant hindLeft = RobotQuadrant.HIND_LEFT;

   @Test
   public void testGetAcrossBodyLeg()
   {
      assertEquals(frontLeft, frontRight.getAcrossBodyQuadrant());
      assertEquals(frontRight, frontLeft.getAcrossBodyQuadrant());
      assertEquals(hindLeft, hindRight.getAcrossBodyQuadrant());
      assertEquals(hindRight, hindLeft.getAcrossBodyQuadrant());
   }

   @Test
   public void testGetAllLegs()
   {
      ArrayList<RobotQuadrant> actualReturn = RobotQuadrant.getAllQuadrants();
      assertEquals("Number of legs", 4, actualReturn.size());
      assertEquals(frontLeft, actualReturn.get(0));
      assertEquals(frontRight, actualReturn.get(1));
      assertEquals(hindRight, actualReturn.get(2));
      assertEquals(hindLeft, actualReturn.get(3));
   }

//   @DeployableTestMethod(duration = 0.1)
//   @Test
//   public void testGetBodyQuadrant()
//   {
//      ReferenceFrame frame = LittleDogFrames.getBodyFrame();
//      assertEquals(frontLeft, getBodyQuadrant(new FramePoint(frame, 1.0, 1.0, 0)));
//      assertEquals(frontRight, getBodyQuadrant(new FramePoint(frame, 1.0, -1.0, 0)));
//      assertEquals(hindRight, getBodyQuadrant(new FramePoint(frame, -1.0, -1.0, 0)));
//      assertEquals(hindLeft, getBodyQuadrant(new FramePoint(frame, -1.0, 1.0, 0)));
//   }
   
//   private static RobotQuadrant getBodyQuadrant(FramePoint point)
//   {
//      point = new FramePoint(point);
////      point.changeFrame(LittleDogFrames.getBodyFrame());
//
//      if (point.getX() >= 0.0)
//      {
//         if (point.getY() >= 0.0)
//            return RobotQuadrant.FRONT_LEFT;
//         else
//            return RobotQuadrant.FRONT_RIGHT;
//      }
//      else
//      {
//         if (point.getY() >= 0.0)
//            return RobotQuadrant.HIND_LEFT;
//         else
//            return RobotQuadrant.HIND_RIGHT;
//      }
//   }

   @Test
   public void testGetDiagonalOppositeLeg()
   {
      assertEquals(hindRight, frontLeft.getDiagonalOppositeQuadrant());
      assertEquals(hindLeft, frontRight.getDiagonalOppositeQuadrant());
      assertEquals(frontLeft, hindRight.getDiagonalOppositeQuadrant());
      assertEquals(frontRight, hindLeft.getDiagonalOppositeQuadrant());
   }

   @Test
   public void testGetLegName()
   {
      assertEquals(frontLeft, RobotQuadrant.getQuadrantName("FRONT_LEFT"));
      assertEquals(frontRight, RobotQuadrant.getQuadrantName("FRONT_RIGHT"));
      assertEquals(hindLeft, RobotQuadrant.getQuadrantName("HIND_LEFT"));
      assertEquals(hindRight, RobotQuadrant.getQuadrantName("HIND_RIGHT"));
   }

   @Test
   public void testGetLegName1()
   {
      assertEquals(frontLeft, RobotQuadrant.getQuadrantNameFromOrdinal(0));
      assertEquals(frontRight, RobotQuadrant.getQuadrantNameFromOrdinal(1));
      assertEquals(hindRight, RobotQuadrant.getQuadrantNameFromOrdinal(2));
      assertEquals(hindLeft, RobotQuadrant.getQuadrantNameFromOrdinal(3));
   }

   @Test
   public void testGetSameSideLeg()
   {
      assertEquals(hindLeft, frontLeft.getSameSideQuadrant());
      assertEquals(hindRight, frontRight.getSameSideQuadrant());
      assertEquals(frontRight, hindRight.getSameSideQuadrant());
      assertEquals(frontLeft, hindLeft.getSameSideQuadrant());
   }

   @Test
   public void testGetShortName()
   {
      assertEquals("FL", frontLeft.getShortName());
      assertEquals("FR", frontRight.getShortName());
      assertEquals("HR", hindRight.getShortName());
      assertEquals("HL", hindLeft.getShortName());
   }

   @Test
   public void testIsLegAFrontLeg()
   {
      assertTrue(frontLeft.isQuadrantInFront());
      assertTrue(frontRight.isQuadrantInFront());
      assertFalse(hindRight.isQuadrantInFront());
      assertFalse(hindLeft.isQuadrantInFront());
   }

   @Test
   public void testIsLegAHindLeg()
   {
      assertFalse(frontLeft.isQuadrantInHind());
      assertFalse(frontRight.isQuadrantInHind());
      assertTrue(hindRight.isQuadrantInHind());
      assertTrue(hindLeft.isQuadrantInHind());
   }

   @Test
   public void testIsLegALeftSideLeg()
   {
      assertTrue(frontLeft.isQuadrantOnLeftSide());
      assertFalse(frontRight.isQuadrantOnLeftSide());
      assertFalse(hindRight.isQuadrantOnLeftSide());
      assertTrue(hindLeft.isQuadrantOnLeftSide());
   }

   @Test
   public void testIsLegARightSideLeg()
   {
      assertFalse(frontLeft.isQuadrantOnRightSide());
      assertTrue(frontRight.isQuadrantOnRightSide());
      assertTrue(hindRight.isQuadrantOnRightSide());
      assertFalse(hindLeft.isQuadrantOnRightSide());
   }
}
