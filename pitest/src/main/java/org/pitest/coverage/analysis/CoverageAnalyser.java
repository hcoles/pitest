package org.pitest.coverage.analysis;

import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.pitest.coverage.CoverageClassVisitor;
import org.pitest.mutationtest.engine.gregor.analysis.DefaultInstructionCounter;
import org.pitest.mutationtest.engine.gregor.analysis.InstructionTrackingMethodVisitor;

import sun.pitest.CodeCoverageStore;

/**
 * Need to count the number of blocks in the method. Storing method as a tree
 * enables a second scan by the instrumenting visitor
 *
 */
public class CoverageAnalyser extends MethodNode {

  private static final int           MAX_SUPPORTED_LOCAL_PROBES = 15;

  private final CoverageClassVisitor parent;
  private final int                  classId;
  private final MethodVisitor        mv;
  private final int                  probeOffset;

  public CoverageAnalyser(final CoverageClassVisitor parent, final int classId,
      final int probeOffset, final MethodVisitor mv, final int access,
      final String name, final String desc, final String signature,
      final String[] exceptions) {
    super(Opcodes.ASM6, access, name, desc, signature, exceptions);
    this.mv = mv;
    this.parent = parent;
    this.classId = classId;
    this.probeOffset = probeOffset;
  }

  @Override
  public void visitEnd() {
    final List<Block> blocks = findRequriedProbeLocations();

    this.parent.registerProbes(blocks.size());
    final int blockCount = blocks.size();
    CodeCoverageStore.registerMethod(this.classId, this.name, this.desc,
        this.probeOffset, (this.probeOffset + blocks.size()) - 1);

    // according to the jvm spec
    // "There must never be an uninitialized class instance in a local variable in code protected by an exception handler"
    // the code to add finally blocks used by the local variable and array based
    // probe approaches is not currently
    // able to meet this guarantee for constructors. Although they appear to
    // work, they are rejected by the
    // java 7 verifier - hence fall back to a simple but slow approach.
    final DefaultInstructionCounter counter = new DefaultInstructionCounter();

    if ((blockCount == 1) || this.name.equals("<init>")) {
      accept(new InstructionTrackingMethodVisitor(
          new SimpleBlockCoverageVisitor(blocks, counter, this.classId,
              this.mv, this.access, this.name, this.desc, this.probeOffset),
              counter));
    } else if ((blockCount <= MAX_SUPPORTED_LOCAL_PROBES) && (blockCount >= 1)) {
      accept(new InstructionTrackingMethodVisitor(
          new LocalVariableCoverageMethodVisitor(blocks, counter, this.classId,
              this.mv, this.access, this.name, this.desc, this.probeOffset),
              counter));
    } else {
      // for now fall back to the naive implementation - could instead use array
      // passing version
      accept(new InstructionTrackingMethodVisitor(
          new ArrayProbeCoverageMethodVisitor(blocks, counter, this.classId,
              this.mv, this.access, this.name, this.desc, this.probeOffset),
              counter));
    }

  }

  private List<Block> findRequriedProbeLocations() {
    return ControlFlowAnalyser.analyze(this);
  }
}
