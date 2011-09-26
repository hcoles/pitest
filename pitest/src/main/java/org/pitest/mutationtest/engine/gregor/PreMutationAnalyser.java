package org.pitest.mutationtest.engine.gregor;

import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

public class PreMutationAnalyser implements ClassVisitor {

  private final PremutationClassInfo classInfo = new PremutationClassInfo();
  private final Set<String>          loggingClasses;

  public PreMutationAnalyser(final Set<String> loggingClasses) {
    this.loggingClasses = loggingClasses;
  }

  public void visit(final int version, final int access, final String name,
      final String signature, final String superName, final String[] interfaces) {

  }

  public void visitSource(final String source, final String debug) {

  }

  public void visitOuterClass(final String owner, final String name,
      final String desc) {

  }

  public AnnotationVisitor visitAnnotation(final String desc,
      final boolean visible) {
    return null;
  }

  public void visitAttribute(final Attribute attr) {

  }

  public void visitInnerClass(final String name, final String outerName,
      final String innerName, final int access) {

  }

  public FieldVisitor visitField(final int access, final String name,
      final String desc, final String signature, final Object value) {
    return null;

  }

  public MethodVisitor visitMethod(final int access, final String name,
      final String desc, final String signature, final String[] exceptions) {
    return new PreMutationMethodAnalyzer(this.loggingClasses, this.classInfo);
  }

  public void visitEnd() {

  }

  public PremutationClassInfo getClassInfo() {
    return this.classInfo;
  }

}
