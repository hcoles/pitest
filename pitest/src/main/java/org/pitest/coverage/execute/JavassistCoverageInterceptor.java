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
package org.pitest.coverage.execute;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.pitest.bytecode.FrameOptions;
import org.pitest.classinfo.ComputeClassWriter;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.coverage.CoverageClassVisitor;
import org.pitest.reflection.Reflection;
import org.pitest.util.IsolationUtils;
import org.pitest.util.StreamUtil;
import org.pitest.util.Unchecked;

import sun.pitest.CodeCoverageStore;

public final class JavassistCoverageInterceptor {
  
  private static final Map<String, String> COMPUTE_CACHE = new ConcurrentHashMap<String, String>();

  private JavassistCoverageInterceptor() {

  }

  public static InputStream openClassfile(final Object classPath, // NO_UCD
      final String name) {

    try {
      if (isInstrumentedClass(name)) {
        byte[] bs = getOriginalBytes(classPath, name);
        return new ByteArrayInputStream(
            transformBytes(IsolationUtils.getContextClassLoader(), name, bs));
      } else {
        return returnNormalBytes(classPath, name);
      }
    } catch (IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }

  }

  private static byte[] getOriginalBytes(final Object classPath,
      final String name) throws IOException {
    InputStream is = returnNormalBytes(classPath,name);
    byte[] bs = StreamUtil.streamToByteArray(is);
    is.close();
    return bs;
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
    reader.accept(new CoverageClassVisitor(id, writer),
        ClassReader.EXPAND_FRAMES);
    return writer.toByteArray();
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

  private static boolean isInstrumentedClass(final String name) {
    return true;
  }


}
