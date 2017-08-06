package org.pitest.mutationtest.engine.gregor;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Disables mutations in the conditional statements generated
 * by java compilers when switching on a string value.
 * 
 * Disable logic looks for a call to hashcode followed immediately by
 * a tableswitch or lookupswitch.
 * 
 * Mutation is renabled when the switch block is complete or a new line of code
 * is encountered.
 *
 */
public class AvoidStringSwitchedMethodAdapter extends MethodVisitor {

  private static final String   DISABLE_REASON = "STRING_SWITCH";

  private final MutationContext context;
  
  // Set to indicate a call to hashcode has been encountered.
  // Unset when other instructions hit so that we can detect if
  // calls directly follow each other
  private boolean               hashCodeWasLastCall;
  
  private Label                 end;

  public AvoidStringSwitchedMethodAdapter(final MutationContext context,
      final MethodVisitor delegateMethodVisitor) {
    super(Opcodes.ASM6, delegateMethodVisitor);
    this.context = context;
  }

  @Override
  public void visitMethodInsn(final int opcode, final String owner,
      final String name, final String desc, boolean itf) {
    if ((opcode == Opcodes.INVOKEVIRTUAL) && "java/lang/String".equals(owner)
        && "hashCode".equals(name)) {
      hashCodeWasLastCall = true;
    } else {
      hashCodeWasLastCall = false;
    }
    super.visitMethodInsn(opcode, owner, name, desc, itf);
  }

  @Override
  public void visitLineNumber(int line, Label start) {
    super.visitLineNumber(line, start);
    // if compiler emits a line number switch statement (probably) contains user
    // generated code
    enableMutation();
  }
    
  @Override
  public void visitTableSwitchInsn(int min, int max, Label dflt,
      Label... labels) {

    // delegate first so switch statements will be mutated
    super.visitTableSwitchInsn(min, max, dflt, labels);

    if (hashCodeWasLastCall) {
      this.context.disableMutations(DISABLE_REASON);
      end = dflt;
    }
  }

  @Override
  public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
    // delegate first so switch statements will be mutated
    super.visitLookupSwitchInsn(dflt, keys, labels);

    if (hashCodeWasLastCall) {
      this.context.disableMutations(DISABLE_REASON);
      end = dflt;
    }
  }

  @Override
  public void visitLabel(final Label label) {
    // delegate to child first to ensure visitLabel not in scope for mutation
    super.visitLabel(label);
    if (this.end == label) {
      enableMutation();
    }
  }
  
  @Override
  public void visitJumpInsn(int opcode, Label label) {
    hashCodeWasLastCall = false;
    super.visitJumpInsn(opcode, label);
  }

  @Override
  public void visitIntInsn(int opcode, int operand) {
    hashCodeWasLastCall = false;
    super.visitIntInsn(opcode, operand);
  }
  
  @Override
  public void visitInsn(int opcode) {
    hashCodeWasLastCall = false;
    super.visitInsn(opcode);
  }

  @Override
  public void visitEnd() {
    super.visitEnd();
    enableMutation();
  }

  private void enableMutation() {
    this.context.enableMutatations(DISABLE_REASON);
    this.end = null;
    hashCodeWasLastCall = false;
  }

}
