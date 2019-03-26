package org.pitest.coverage.analysis;

import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.MethodNode;
import org.pitest.bytecode.ASMVersion;
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
    super(ASMVersion.ASM_VERSION, access, name, desc, signature, exceptions);
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

    final DefaultInstructionCounter counter = new DefaultInstructionCounter();
    accept(new InstructionTrackingMethodVisitor(
        new ArrayProbeCoverageMethodVisitor(blocks, counter, this.classId,
            this.mv, this.access, parent.getClassName(), this.name, this.desc,
            this.probeOffset), counter));
  }

  private List<Block> findRequriedProbeLocations() {
    return ControlFlowAnalyser.analyze(this);
  }
}
