package org.pitest.mutationtest.build.intercept.jspecify;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassPathRoot;
import org.pitest.util.Unchecked;

/**
 * Finds the module-info for the module that a class belongs to.
 */
interface ModuleInfoSource {

  Optional<byte[]> moduleInfoFor(ClassName clazz);

  /**
   * Looks for module-info only in the classpath roots that contain the class,
   * so that the module-info of another module (e.g. a dependency, or a sibling
   * module in a multi module build) is never used.
   */
  static ModuleInfoSource fromClassPath(ClassPath classPath) {
    return clazz -> {
      final ClassPath owningRoots = classPath.getComponent(root -> contains(root, clazz));
      try {
        return Optional.ofNullable(owningRoots.getClassData("module-info"));
      } catch (final IOException ex) {
        throw Unchecked.translateCheckedException(ex);
      }
    };
  }

  static boolean contains(ClassPathRoot root, ClassName clazz) {
    try (InputStream is = root.getData(clazz.asJavaName())) {
      return is != null;
    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

}
