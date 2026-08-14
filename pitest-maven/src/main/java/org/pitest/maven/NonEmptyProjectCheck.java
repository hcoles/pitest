package org.pitest.maven;

import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

public class NonEmptyProjectCheck implements Predicate<MavenProject> {

  @SuppressWarnings("unchecked")
  @Override
  public boolean test(MavenProject project) {
    return project.getTestCompileSourceRoots().stream().anyMatch(this::exists)
        && project.getCompileSourceRoots().stream().anyMatch(this::exists);
  }
  
  private boolean exists(String root) {
    var p = Path.of(root);
      try {
          return Files.isDirectory(p) && isPopulated(p);
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
  }

  private static boolean isPopulated(Path p) throws IOException {
    try (var list = Files.list(p)) {
      return list.findAny().isPresent();
    }
  }

}
