package org.pitest.mutationtest.mocksupport;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.function.Function;
import java.util.function.Predicate;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.pitest.bytecode.FrameOptions;

public class BendJavassistToMyWillTransformer implements ClassFileTransformer {

  private final Predicate<String> filter;
  private final Function<ClassWriter,ClassVisitor> transformation;

  public BendJavassistToMyWillTransformer(final Predicate<String> filter, Function<ClassWriter,ClassVisitor> transformation) {
    this.filter = filter;
    this.transformation = transformation;
  }

  @Override
  public byte[] transform(final ClassLoader loader, final String className,
      final Class<?> classBeingRedefined,
      final ProtectionDomain protectionDomain, final byte[] classfileBuffer)
          throws IllegalClassFormatException {

    if (shouldInclude(className)) {

      final ClassReader reader = new ClassReader(classfileBuffer);
      final ClassWriter writer = new ClassWriter(
          FrameOptions.pickFlags(classfileBuffer));

      reader.accept(this.transformation.apply(writer),
          ClassReader.EXPAND_FRAMES);
      return writer.toByteArray();
    } else {
      return null;
    }
  }

  private boolean shouldInclude(final String className) {
    return this.filter.test(className);
  }

}
