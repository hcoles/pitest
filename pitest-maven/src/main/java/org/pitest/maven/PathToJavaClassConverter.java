package org.pitest.maven;

import java.io.File;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Converts paths to java class names globs if they are within the supplied
 * source root. Globs include wildcards to catch any inner classes.
 *
 * This conversion will not pick up non public, non inner classes defined in a
 * source file not matching the class name. To cover this corner case would need
 * to instead scan the byte code on the classpath and see if it had been tagged
 * with the supplied filenames.
 *
 */
class PathToJavaClassConverter implements Function<String, Stream<String>> {

  private final String sourceRoot;

  PathToJavaClassConverter(final String sourceRoot) {
    this.sourceRoot = sourceRoot;
  }

  @Override
  public Stream<String> apply(final String a) {
    final File f = new File(a);
    final String modifiedFilePath = f.getAbsolutePath();
    final String fileName = f.getName();

    if (modifiedFilePath.startsWith(this.sourceRoot)
        && (fileName.indexOf('.') != -1)) {
      return createClassGlobFromFilePath(this.sourceRoot, modifiedFilePath);
    }
    return Stream.empty();

  }

  private Stream<String> createClassGlobFromFilePath(final String sourceRoot,
      final String modifiedFilePath) {
    final String rootedPath = modifiedFilePath.substring(sourceRoot.length() + 1);
    // some scms report paths in portable format, some in os specific format (i
    // think)
    // replace both possibilities regardless of host os
    return Stream.of(stripFileExtension(rootedPath).replace('/',
        '.').replace('\\', '.')
        + "*");
  }

  private String stripFileExtension(final String rootedPath) {
    return rootedPath.substring(0, rootedPath.lastIndexOf("."));
  }

}
