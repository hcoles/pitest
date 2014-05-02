package org.pitest.coverage.codeassist;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Need to count the number of lines in the method. Storing method as a tree
 * enables a second scan by the instrumenting visitor
 * 
 */
public class CoverageAnalyser extends MethodNode {

  private static final int    MAX_SUPPORTED_LOCAL_PROBES = 15;
  private final LineTracker   lineTracker;
  private final int           classId;
  private final MethodVisitor mv;
  private final int           probeOffset;

  public CoverageAnalyser(final LineTracker lineTracker, final int classId,
      final int probeOffset, final MethodVisitor mv, final int access,
      final String name, final String desc, final String signature,
      final String[] exceptions) {
    super(Opcodes.ASM5,access, name, desc, signature, exceptions);
    this.mv = mv;
    this.lineTracker = lineTracker;
    this.classId = classId;
    this.probeOffset = probeOffset;
  }

  @Override
  public void visitEnd() {
    final int numberOfProbes = countRequiredProbes();

    // according to the jvm spec
    // "There must never be an uninitialized class instance in a local variable in code protected by an exception handler"
    // the code to add finally blocks used by the local variable and array based
    // probe approaches is not currently
    // able to meet this guarantee for constructors. Although they appear to
    // work, they are rejected by the
    // java 7 verifier - hence fall back to a simple but slow approach.
    if ((numberOfProbes == 1) || this.name.equals("<init>")) {
      accept(new SimpleCoverageVisitor(this.lineTracker, this.classId, this.mv,
          this.access, this.name, this.desc, this.probeOffset));
    }

    else if ((numberOfProbes <= MAX_SUPPORTED_LOCAL_PROBES)
        && (numberOfProbes >= 1)) {
      accept(new LocalVariableCoverageMethodVisitor(this.lineTracker,
          this.classId, this.mv, this.access, this.name, this.desc,
          numberOfProbes, this.probeOffset));
    } else {
      accept(new CoverageMethodVisitor(this.lineTracker, this.classId, this.mv,
          this.access, this.name, this.desc, numberOfProbes, this.probeOffset));
    }

  }

  private int countRequiredProbes() {
    int count = 0;
    for (int i = 0; i < this.instructions.size(); i++) {
      final AbstractInsnNode ins = this.instructions.get(i);
      if (ins instanceof LineNumberNode) {
        count++;
      }
    }
    return count;
  }
}
