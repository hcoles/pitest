package org.pitest.coverage;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.pitest.bytecode.FrameOptions;
import org.pitest.classinfo.ComputeClassWriter;
import org.pitest.classpath.ClassloaderByteArraySource;

import sun.pitest.CodeCoverageStore;

public class CoverageTransformer implements ClassFileTransformer {

  private final Predicate<String>   filter;
  private final Map<String, String> computeCache = new ConcurrentHashMap<>();

  public CoverageTransformer(final Predicate<String> filter) {
    this.filter = filter;
  }

  @Override
  public byte[] transform(final ClassLoader loader, final String className,
      final Class<?> classBeingRedefined,
      final ProtectionDomain protectionDomain, final byte[] classfileBuffer)
          throws IllegalClassFormatException {
    final boolean include = shouldInclude(className);
    if (include) {
      try {
        return transformBytes(pickLoader(loader), className, classfileBuffer);
      } catch (final RuntimeException t) {
        System.err.println("RuntimeException while transforming  " + className);
        t.printStackTrace();
        throw t;
      }
    } else {
      return null;
    }
  }

  private byte[] transformBytes(final ClassLoader loader,
      final String className, final byte[] classfileBuffer) {
    final ClassReader reader = new ClassReader(classfileBuffer);

    /*
    Make sure that this class has not already been instrumented for coverage
    generation. It is possible that some other bytecode instrumentation tool
    would try to redefine a class (that we already added coverage tracking to),
    in which case we will just allow that previous coverage tracking to stand.
     */
    final ClassWriter writer = new ComputeClassWriter(
        new ClassloaderByteArraySource(loader), this.computeCache,
        FrameOptions.pickFlags(classfileBuffer));

    final int id = CodeCoverageStore.registerClass(className);
    try {
      reader.accept(new CoverageClassVisitor(id, writer),
          ClassReader.EXPAND_FRAMES);
      return writer.toByteArray();
    } catch (AlreadyInstrumentedException ex) {
      return null;
    }
  }

  private boolean shouldInclude(final String className) {
    return this.filter.test(className);
  }

  private ClassLoader pickLoader(ClassLoader loader) {
    if (loader != null) {
      return loader;
    }
    return ClassLoader.getSystemClassLoader();
  }

}
