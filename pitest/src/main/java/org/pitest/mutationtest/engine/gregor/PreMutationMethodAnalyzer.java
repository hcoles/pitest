package org.pitest.mutationtest.engine.gregor;

import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;

public class PreMutationMethodAnalyzer implements MethodVisitor {

  private final Set<String>          loggingClasses;

  private int                        currentLineNumber;
  private final PremutationClassInfo classInfo;

  public PreMutationMethodAnalyzer(final Set<String> loggingClasses,
      final PremutationClassInfo classInfo) {
    this.classInfo = classInfo;
    this.loggingClasses = loggingClasses;
  }

  public void visitMethodInsn(final int opcode, final String owner,
      final String name, final String desc) {

    if (FCollection.contains(this.loggingClasses, matches(owner))) {
      this.classInfo.registerLoggingLine(this.currentLineNumber);
    }
  }

  private F<String, Boolean> matches(final String owner) {
    return new F<String, Boolean>() {
      public Boolean apply(final String a) {
        return owner.startsWith(a);
      }

    };
  }

  public void visitLineNumber(final int line, final Label start) {
    this.currentLineNumber = line;
  }

  public AnnotationVisitor visitAnnotation(final String arg0, final boolean arg1) {

    return null;
  }

  public AnnotationVisitor visitAnnotationDefault() {

    return null;
  }

  public void visitAttribute(final Attribute arg0) {

  }

  public void visitCode() {

  }

  public void visitEnd() {

  }

  public void visitFieldInsn(final int arg0, final String arg1,
      final String arg2, final String arg3) {

  }

  public void visitFrame(final int arg0, final int arg1, final Object[] arg2,
      final int arg3, final Object[] arg4) {

  }

  public void visitIincInsn(final int arg0, final int arg1) {

  }

  public void visitInsn(final int arg0) {

  }

  public void visitIntInsn(final int arg0, final int arg1) {

  }

  public void visitJumpInsn(final int arg0, final Label arg1) {

  }

  public void visitLabel(final Label arg0) {

  }

  public void visitLdcInsn(final Object arg0) {

  }

  public void visitLocalVariable(final String arg0, final String arg1,
      final String arg2, final Label arg3, final Label arg4, final int arg5) {

  }

  public void visitLookupSwitchInsn(final Label arg0, final int[] arg1,
      final Label[] arg2) {

  }

  public void visitMaxs(final int arg0, final int arg1) {

  }

  public void visitMultiANewArrayInsn(final String arg0, final int arg1) {

  }

  public AnnotationVisitor visitParameterAnnotation(final int arg0,
      final String arg1, final boolean arg2) {

    return null;
  }

  public void visitTableSwitchInsn(final int arg0, final int arg1,
      final Label arg2, final Label[] arg3) {

  }

  public void visitTryCatchBlock(final Label arg0, final Label arg1,
      final Label arg2, final String arg3) {

  }

  public void visitTypeInsn(final int arg0, final String arg1) {

  }

  public void visitVarInsn(final int arg0, final int arg1) {

  }

}
