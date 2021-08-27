package org.pitest.mutationtest.engine.gregor.mutators;

import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 * Creates "mutants" that contain no changes at all possible mutation
 * points.
 *
 * Intended for testing purposes only.
 *
 */
public class NullMutateEverything implements MethodMutatorFactory {

  @Override
  public MethodVisitor create(MutationContext context, MethodInfo methodInfo,
      MethodVisitor methodVisitor) {
    return new MutateEveryThing(this, context, methodVisitor);
  }

  @Override
  public String getGloballyUniqueId() {
    return "mutateallthethings";
  }

  @Override
  public String getName() {
    return "mutateeverything";
  }

  public static List<MethodMutatorFactory> asList() {
    return Arrays.asList(new NullMutateEverything());
  }

}

class MutateEveryThing extends MethodVisitor {
  private final MethodMutatorFactory factory;
  private final MutationContext      context;

  MutateEveryThing(final MethodMutatorFactory factory,
      final MutationContext context,
      final MethodVisitor delegateMethodVisitor) {
    super(ASMVersion.ASM_VERSION, delegateMethodVisitor);
    this.factory = factory;
    this.context = context;
  }

  @Override
  public void visitIincInsn(final int var, final int increment) {
    mutate("visitIincInsn");
    super.visitIincInsn(var, increment);
  }

  @Override
  public void visitInsn(int opcode) {
    if (opcode != Opcodes.RETURN) {
      mutate("visitInsn", opcode);
    }
    super.visitInsn(opcode);
  }

  @Override
  public void visitIntInsn(int opcode, int operand) {
    mutate("visitIntInsn", opcode);
    super.visitIntInsn(opcode,operand);
  }

  @Override
  public void visitVarInsn(int opcode, int var) {
    mutate("visitVarInsn", opcode);
    super.visitVarInsn(opcode,var);
  }

  @Override
  public void visitTypeInsn(int opcode, String type) {
    mutate("visitTypeInsn", opcode);
    super.visitTypeInsn(opcode, type);
  }

  @Override
  public void visitFieldInsn(int opcode, String owner, String name,
      String desc) {
    mutate("visitFieldInsn", opcode);
    super.visitFieldInsn(opcode, owner, name, desc);
  }

  @Override
  public void visitMethodInsn(int opcode, String owner, String name,
      String desc, boolean itf) {
    mutate("visitMethodInsn", opcode);
    super.visitMethodInsn(opcode, owner, name, desc, itf);
  }

  @Override
  public void visitInvokeDynamicInsn(String name, String desc, Handle bsm,
      Object... bsmArgs) {
    mutate("visitInvokeDynamicInsn");
    super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
  }

  @Override
  public void visitJumpInsn(int opcode, Label label) {
    mutate("visitJumpInsn", opcode);
    super.visitJumpInsn(opcode, label);
  }

  @Override
  public void visitLdcInsn(Object cst) {
    mutate("visitLdcInsn");
    super.visitLdcInsn(cst);
  }

  @Override
  public void visitTableSwitchInsn(int min, int max, Label dflt,
      Label... labels) {
    mutate("visitTableSwitchInsn");
    super.visitTableSwitchInsn(min, max, dflt, labels);
  }

  @Override
  public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
    mutate("visitLookupSwitchInsn");
    super.visitLookupSwitchInsn(dflt, keys, labels);
  }

  @Override
  public void visitMultiANewArrayInsn(String desc, int dims) {
    mutate("visitMultiANewArrayInsn");
    super.visitMultiANewArrayInsn(desc, dims);
  }

  @Override
  public void visitTryCatchBlock(Label start, Label end, Label handler,
      String type) {
    // Can't mutate try catch blocks as they are not modelled as an instruction in ASM
    super.visitTryCatchBlock(start, end, handler, type);
  }

  private void mutate(String string, int opcode) {
    mutate("Null mutation in " + string + " with " + opcode);
  }

  private void mutate(String string) {
    this.context.registerMutation(this.factory, string);
  }

}
