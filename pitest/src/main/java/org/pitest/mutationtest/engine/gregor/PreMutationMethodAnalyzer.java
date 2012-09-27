package org.pitest.mutationtest.engine.gregor;

import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;

public class PreMutationMethodAnalyzer extends MethodVisitor {

  private final Set<String>          loggingClasses;

  private int                        currentLineNumber;
  private final PremutationClassInfo classInfo;

  public PreMutationMethodAnalyzer(final Set<String> loggingClasses,
      final PremutationClassInfo classInfo) {
    super(Opcodes.ASM4);
    this.classInfo = classInfo;
    this.loggingClasses = loggingClasses;
  }

  @Override
  public void visitMethodInsn(final int opcode, final String owner,
      final String name, final String desc) {

    if (FCollection.contains(this.loggingClasses, matches(owner))) {
      this.classInfo.registerLoggingLine(this.currentLineNumber);
    }
  }

  private static F<String, Boolean> matches(final String owner) {
    return new F<String, Boolean>() {
      public Boolean apply(final String a) {
        return owner.startsWith(a);
      }

    };
  }

  @Override
  public void visitLineNumber(final int line, final Label start) {
    this.currentLineNumber = line;
  }

  @Override
  public AnnotationVisitor visitAnnotation(final String arg0, final boolean arg1) {

    return null;
  }

  @Override
  public AnnotationVisitor visitAnnotationDefault() {

    return null;
  }

  @Override
  public void visitAttribute(final Attribute arg0) {

  }

  @Override
  public void visitCode() {

  }

  @Override
  public void visitEnd() {

  }

  @Override
  public void visitFieldInsn(final int arg0, final String arg1,
      final String arg2, final String arg3) {

  }

  @Override
  public void visitFrame(final int arg0, final int arg1, final Object[] arg2,
      final int arg3, final Object[] arg4) {

  }

  @Override
  public void visitIincInsn(final int arg0, final int arg1) {

  }

  @Override
  public void visitInsn(final int arg0) {

  }

  @Override
  public void visitIntInsn(final int arg0, final int arg1) {

  }

  @Override
  public void visitJumpInsn(final int arg0, final Label arg1) {

  }

  @Override
  public void visitLabel(final Label arg0) {

  }

  @Override
  public void visitLdcInsn(final Object arg0) {

  }

  @Override
  public void visitLocalVariable(final String arg0, final String arg1,
      final String arg2, final Label arg3, final Label arg4, final int arg5) {

  }

  @Override
  public void visitLookupSwitchInsn(final Label arg0, final int[] arg1,
      final Label[] arg2) {

  }

  @Override
  public void visitMaxs(final int arg0, final int arg1) {

  }

  @Override
  public void visitMultiANewArrayInsn(final String arg0, final int arg1) {

  }

  @Override
  public AnnotationVisitor visitParameterAnnotation(final int arg0,
      final String arg1, final boolean arg2) {

    return null;
  }

  @Override
  public void visitTableSwitchInsn(final int arg0, final int arg1,
      final Label arg2, final Label... labels) {

  }

  @Override
  public void visitTryCatchBlock(final Label arg0, final Label arg1,
      final Label arg2, final String arg3) {

  }

  @Override
  public void visitTypeInsn(final int arg0, final String arg1) {

  }

  @Override
  public void visitVarInsn(final int arg0, final int arg1) {

  }

}
