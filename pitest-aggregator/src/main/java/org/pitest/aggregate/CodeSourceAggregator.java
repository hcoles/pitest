package org.pitest.aggregate;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassFilter;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassPathRoot;
import org.pitest.classpath.CodeSource;
import org.pitest.classpath.DirectoryClassPathRoot;
import org.pitest.classpath.PathFilter;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.config.DefaultCodePathPredicate;
import org.pitest.mutationtest.config.DefaultDependencyPathPredicate;
import org.pitest.util.Glob;

class CodeSourceAggregator {

  private final Collection<File> compiledCodeDirectories;

  CodeSourceAggregator(final Collection<File> compiledCodeDirectories) {
    this.compiledCodeDirectories = Collections.unmodifiableCollection(compiledCodeDirectories);
  }

  public CodeSource createCodeSource() {
    return new CodeSource(createProjectClassPaths());
  }

  private ProjectClassPaths createProjectClassPaths() {
    final ClassPath classPath = new ClassPath(compiledCodeDirectories);
    final Predicate<String> classPredicate = createClassPredicate();
    final Predicate<ClassPathRoot> pathPredicate = new DefaultCodePathPredicate();
    return new ProjectClassPaths(classPath, new ClassFilter(classPredicate, classPredicate),
        new PathFilter(pathPredicate, Prelude.not(new DefaultDependencyPathPredicate())));
  }

  private Predicate<String> createClassPredicate() {
    final Collection<String> classes = new HashSet<>();
    for (final File buildOutputDirectory : compiledCodeDirectories) {
      if (buildOutputDirectory.exists()) {
        final DirectoryClassPathRoot dcRoot = new DirectoryClassPathRoot(buildOutputDirectory);
        classes.addAll(FCollection.map(dcRoot.classNames(), toPredicate()));
      }
    }
    return Prelude.or(FCollection.map(classes, Glob.toGlobPredicate()));
  }

  private F<String, String> toPredicate() {
    return new F<String, String>() {
      @Override
      public String apply(final String a) {
        return ClassName.fromString(a).getPackage().asJavaName() + ".*";
      }
    };
  }
  
}
