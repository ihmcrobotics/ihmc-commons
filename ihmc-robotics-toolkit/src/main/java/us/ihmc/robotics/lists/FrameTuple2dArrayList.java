package us.ihmc.robotics.lists;

import java.util.List;

import us.ihmc.euclid.referenceFrame.FramePoint2D;
import us.ihmc.euclid.referenceFrame.FrameTuple2D;
import us.ihmc.euclid.referenceFrame.FrameVector2D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.tuple2D.interfaces.Tuple2DBasics;

public class FrameTuple2dArrayList<T extends FrameTuple2D<?, ?>> extends RecyclingArrayList<T>
{
   private FrameTuple2dArrayList(Class<T> clazz)
   {
      super(clazz);
   }

   private FrameTuple2dArrayList(int initialSize, Class<T> clazz)
   {
      super(initialSize, clazz);
   }

   public void setOrCreate(int i, FrameTuple2D<?, ?> frameTuple2d)
   {
      if (i >= size)
      {
         size = i + 1;
         ensureCapacity(size);
      }
      unsafeGet(i).setIncludingFrame(frameTuple2d);
   }

   public void set(int i, FrameTuple2D<?, ?> frameTuple2d)
   {
      get(i).setIncludingFrame(frameTuple2d);
   }

   private void unsafeSet(int i, FrameTuple2D<?, ?> frameTuple2d)
   {
      unsafeGet(i).setIncludingFrame(frameTuple2d);
   }

   private void unsafeSet(int i, ReferenceFrame referenceFrame, Tuple2DBasics tuple2d)
   {
      unsafeGet(i).setIncludingFrame(referenceFrame, tuple2d);
   }

   public void copyFromListAndTrimSize(FrameTuple2dArrayList<?> otherList)
   {
      ensureCapacity(otherList.size());
      size = otherList.size;

      for (int i = 0; i < size; i++)
      {
         unsafeSet(i, otherList.unsafeGet(i));
      }
   }

   public void copyFromListAndTrimSize(List<? extends FrameTuple2D<?, ?>> otherList)
   {
      ensureCapacity(otherList.size());
      size = otherList.size();

      for (int i = 0; i < size; i++)
      {
         unsafeSet(i, otherList.get(i));
      }
   }

   public void copyFromPoint2dListAndTrimSize(ReferenceFrame referenceFrame, List<? extends Tuple2DBasics> otherList)
   {
      ensureCapacity(otherList.size());
      size = otherList.size();

      for (int i = 0; i < size; i++)
      {
         unsafeSet(i, referenceFrame, otherList.get(i));
      }
   }

   public static FrameTuple2dArrayList<FramePoint2D> createFramePoint2dArrayList()
   {
      return new FrameTuple2dArrayList<>(FramePoint2D.class);
   }

   public static FrameTuple2dArrayList<FrameVector2D> createFrameVector2dArrayList()
   {
      return new FrameTuple2dArrayList<>(FrameVector2D.class);
   }

   public static FrameTuple2dArrayList<FramePoint2D> createFramePoint2dArrayList(int initialSize)
   {
      return new FrameTuple2dArrayList<>(initialSize, FramePoint2D.class);
   }

   public static FrameTuple2dArrayList<FrameVector2D> createFrameVector2dArrayList(int initialSize)
   {
      return new FrameTuple2dArrayList<>(initialSize, FrameVector2D.class);
   }
}
