package org.pitest;

import java.io.File;
import java.util.Collections;

import org.pitest.functional.F;

/**
 * Converts paths to java class names globs if they are within the supplied
 * source root.
 * 
 * This conversion will not pick up non public, non inner classes defined in a
 * source file not matching the class name. To cover this corner case would need
 * to instead scan the byte code on the classpath and see if it had been tagged
 * with the supplied filenames.
 * 
 */
class PathToJavaClassConverter implements F<String, Iterable<String>> {

  private final String sourceRoot;

  PathToJavaClassConverter(final String sourceRoot) {
    this.sourceRoot = sourceRoot;
  }

  public Iterable<String> apply(final String a) {
    final File f = new File(a);
    final String modifiedFilePath = f.getAbsolutePath();

    if (modifiedFilePath.startsWith(this.sourceRoot)) {
      return createClassGlobFromFilePath(this.sourceRoot, modifiedFilePath);
    }
    return Collections.emptyList();

  }

  private Iterable<String> createClassGlobFromFilePath(final String sourceRoot,
      final String modifiedFilePath) {
    final String rootedPath = modifiedFilePath.substring(
        sourceRoot.length() + 1, modifiedFilePath.length());
    return Collections.singleton(stripFileExtension(rootedPath).replace('/',
        '.'));
  }

  private String stripFileExtension(final String rootedPath) {
    return rootedPath.substring(0, rootedPath.lastIndexOf("."));
  }

}
