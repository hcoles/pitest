/*
 * Copyright 2011 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.mocksupport;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.pitest.bytecode.FrameOptions;
import org.pitest.classinfo.ComputeClassWriter;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.coverage.AlreadyInstrumentedException;
import org.pitest.coverage.CoverageClassVisitor;
import org.pitest.functional.prelude.Prelude;
import org.pitest.reflection.Reflection;
import org.pitest.util.IsolationUtils;
import org.pitest.util.StreamUtil;
import org.pitest.util.Unchecked;
import sun.pitest.CodeCoverageStore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class JavassistCoverageInterceptor {

  private static final Predicate<String> EXCLUDED_CLASSES = excluded();


  private static final Map<String, String> COMPUTE_CACHE = new ConcurrentHashMap<>();

  private JavassistCoverageInterceptor() {

  }

  // not referenced directly, but called from transformed bytecode
  public static InputStream openClassfile(final Object classPath, // NO_UCD
      final String name) {

    try {
        final byte[] bs = getOriginalBytes(classPath, name);
        if (shouldInclude(name)) {
          return new ByteArrayInputStream(transformBytes(IsolationUtils.getContextClassLoader(), name, bs));
        } else {
          return new ByteArrayInputStream(bs);
        }
    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }

  }


  private static boolean shouldInclude(String name) {
    return EXCLUDED_CLASSES.negate().test(name);
  }

  private static byte[] getOriginalBytes(final Object classPath,
      final String name) throws IOException {
    try (InputStream is = returnNormalBytes(classPath,name)) {
      return StreamUtil.streamToByteArray(is);
    }
  }

  private static byte[] transformBytes(final ClassLoader loader,
      final String className, final byte[] classfileBuffer) {
    final ClassReader reader = new ClassReader(classfileBuffer);
    final ClassWriter writer = new ComputeClassWriter(
        new ClassloaderByteArraySource(loader), COMPUTE_CACHE,
        FrameOptions.pickFlags(classfileBuffer));

    // The transformed classes will be given a different id than the one already loaded.
    // Not clear if this is desirable or not. At the point of writing this comment
    // pitest will merge coverage of all classes with the same fully qualified name.
    // If this changes this might become a bug, however it would also probably not be possible
    // to support powermock if this assumption changed, so this code would most likely be deleted.

    final int id = CodeCoverageStore.registerClass(className);
    try {
      reader.accept(new CoverageClassVisitor(id, writer),
          ClassReader.EXPAND_FRAMES);
      return writer.toByteArray();
    } catch (AlreadyInstrumentedException ex) {
      return classfileBuffer;
    }
  }

  private static InputStream returnNormalBytes(final Object classPath,
      final String name) {
    try {
      return (InputStream) Reflection.publicMethod(classPath.getClass(),
          "openClassfile").invoke(classPath, name);
    } catch (final Exception e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

    // Classes loaded by pitest's coverage system are not
    // instrumented unless they are in scope for mutation.
    // They would however be instrumented here (including pitest's own classes).
    // We don't have knowlege of which classes are in scope for mutation here
    // so the best we can do is exclude pitest itself and some common classes.
    private static Predicate<String> excluded() {
      return Prelude.or(
              startsWith("org.objenesis."),
              startsWith("net.bytebuddy."),
              startsWith("javassist."),
              startsWith("org.pitest."),
              startsWith("com.groupcdg."),
              startsWith("com.arcmutate."),
              startsWith("java."),
              startsWith("javax."),
              startsWith("com.sun."),
              startsWith("org.junit."),
              startsWith("org.powermock."),
              startsWith("org.mockito."),
              startsWith("sun."));
    }

    private static Predicate<String> startsWith(String start) {
      return s -> s.startsWith(start);
    }
}
