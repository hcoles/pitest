package org.pitest.mutationtest.engine.gregor;

import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class PreMutationAnalyser extends ClassVisitor {

  private final PremutationClassInfo classInfo = new PremutationClassInfo();
  private final Set<String>          loggingClasses;

  public PreMutationAnalyser(final Set<String> loggingClasses) {
    super(Opcodes.ASM5);
    this.loggingClasses = loggingClasses;
  }

  @Override
  public void visit(final int version, final int access, final String name,
      final String signature, final String superName, final String[] interfaces) {

  }

  @Override
  public void visitSource(final String source, final String debug) {

  }

  @Override
  public void visitOuterClass(final String owner, final String name,
      final String desc) {

  }

  @Override
  public AnnotationVisitor visitAnnotation(final String desc,
      final boolean visible) {
    return null;
  }

  @Override
  public void visitAttribute(final Attribute attr) {

  }

  @Override
  public void visitInnerClass(final String name, final String outerName,
      final String innerName, final int access) {

  }

  @Override
  public FieldVisitor visitField(final int access, final String name,
      final String desc, final String signature, final Object value) {
    return null;

  }

  @Override
  public MethodVisitor visitMethod(final int access, final String name,
      final String desc, final String signature, final String[] exceptions) {
    return new PreMutationMethodAnalyzer(this.loggingClasses, this.classInfo);
  }

  @Override
  public void visitEnd() {

  }

  public PremutationClassInfo getClassInfo() {
    return this.classInfo;
  }

}
