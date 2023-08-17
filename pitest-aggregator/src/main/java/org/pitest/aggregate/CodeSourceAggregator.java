package org.pitest.aggregate;

import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassFilter;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassPathRoot;
import org.pitest.classpath.CodeSource;
import org.pitest.classpath.DirectoryClassPathRoot;
import org.pitest.classpath.PathFilter;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.config.DefaultCodePathPredicate;
import org.pitest.mutationtest.config.DefaultDependencyPathPredicate;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.util.Glob;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class CodeSourceAggregator {

  private final Collection<File> compiledCodeDirectories;
  private final SettingsFactory settings;

  CodeSourceAggregator(SettingsFactory settings, Collection<File> compiledCodeDirectories) {
    this.settings = settings;
    this.compiledCodeDirectories = Collections.unmodifiableCollection(compiledCodeDirectories);
  }

  public CodeSource createCodeSource() {
    return settings.createCodeSource(createProjectClassPaths());
  }

  public ProjectClassPaths createProjectClassPaths() {
    final ClassPath classPath = new ClassPath(this.compiledCodeDirectories);
    final Predicate<String> classPredicate = createClassPredicate();
    final Predicate<ClassPathRoot> pathPredicate = new DefaultCodePathPredicate();
    return new ProjectClassPaths(classPath, new ClassFilter(classPredicate, classPredicate),
        new PathFilter(pathPredicate, Prelude.not(new DefaultDependencyPathPredicate())));
  }

  private Predicate<String> createClassPredicate() {
    final Collection<String> classes = new HashSet<>();
    for (final File buildOutputDirectory : this.compiledCodeDirectories) {
      if (buildOutputDirectory.exists()) {
        final DirectoryClassPathRoot dcRoot = new DirectoryClassPathRoot(buildOutputDirectory);
        classes.addAll(dcRoot.classNames().stream()
                .map(toPredicate())
                .collect(Collectors.toList()));
      }
    }
    return Prelude.or(classes.stream()
            .map(Glob.toGlobPredicate())
            .collect(Collectors.toList()));
  }

  private Function<String, String> toPredicate() {
    return a -> ClassName.fromString(a).getPackage().asJavaName() + ".*";
  }

}
