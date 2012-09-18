package org.pitest.coverage;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.pitest.boot.CodeCoverageStore;
import org.pitest.coverage.codeassist.CoverageClassVisitor;
import org.pitest.functional.predicate.Predicate;

public class CoverageTransformer implements ClassFileTransformer {

  private final Predicate<String> filter;

  public CoverageTransformer(final Predicate<String> filter) {
    this.filter = filter;
  }

  public byte[] transform(final ClassLoader loader, final String className,
      final Class<?> classBeingRedefined,
      final ProtectionDomain protectionDomain, final byte[] classfileBuffer)
      throws IllegalClassFormatException {
    final boolean include = shouldInclude(className);
    if (include) {
      final ClassReader reader = new ClassReader(classfileBuffer);
      final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

      final int id = CodeCoverageStore.registerClass(className);
      reader.accept(new CoverageClassVisitor(id, writer),
          ClassReader.EXPAND_FRAMES);
      return writer.toByteArray();
    } else {
      return null;
    }
  }

  private boolean shouldInclude(final String className) {
    return this.filter.apply(className);
  }

}
