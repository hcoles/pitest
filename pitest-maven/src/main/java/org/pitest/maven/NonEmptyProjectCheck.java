package org.pitest.maven;

import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class NonEmptyProjectCheck implements Predicate<MavenProject> {

  @SuppressWarnings("unchecked")
  @Override
  public boolean test(MavenProject project) {
    var baseDir = project.getBasedir() != null ? project.getBasedir() : new File("");
    return augmentTests(baseDir, project.getTestCompileSourceRoots()).stream().anyMatch(this::exists)
        && augmentMain(baseDir, project.getCompileSourceRoots()).stream().anyMatch(this::exists);
  }

  // maven projects for other jvm languages are often not properly configured
  // and don't declare their source directories
  private List<String> augmentTests(File basedir, List<String> existing) {
    var groovy = basedir.toPath().resolve("src/test/groovy");
    var updated = new ArrayList<>(existing);
    updated.add(groovy.toAbsolutePath().toString());
    return updated;
  }

  private List<String> augmentMain(File basedir, List<String> existing) {
    var groovy = basedir.toPath().resolve("src/main/groovy");
    var updated = new ArrayList<>(existing);
    updated.add(groovy.toAbsolutePath().toString());
    return updated;
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
