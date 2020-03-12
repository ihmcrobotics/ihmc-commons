package us.ihmc.commons.nio;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;

public interface PathVisitor
{
   /**
    * This method is called when a Path is visited.
    *
    * @param path the Path being visited
    * @param pathType the type of Path being visited (file or directory)
    * @return fileVisitResult CONTINUE, SKIP_SIBLINGS, SKIP_SUBTREE, or TERMINATE
    */
   FileVisitResult visitPath(Path path, BasicPathVisitor.PathType pathType);
}
