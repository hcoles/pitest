package org.pitest.maven;

import java.io.File;

import org.apache.maven.project.MavenProject;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;

public class NonEmptyProjectCheck implements Predicate<MavenProject> {

  @SuppressWarnings("unchecked")
  @Override
  public Boolean test(MavenProject project) {
    return FCollection.contains(project.getTestCompileSourceRoots(), exists()) 
        && FCollection.contains(project.getCompileSourceRoots(), exists());
  }
  
  private Predicate<String> exists() {
    return new Predicate<String>() {
      @Override
      public Boolean test(String root) {
        return new File(root).exists();
      }
    };
  }

}
