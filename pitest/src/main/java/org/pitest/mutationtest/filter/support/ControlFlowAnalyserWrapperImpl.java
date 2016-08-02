package org.pitest.mutationtest.filter.support;

import java.util.List;

import org.objectweb.asm.tree.MethodNode;
import org.pitest.coverage.analysis.Block;
import org.pitest.coverage.analysis.ControlFlowAnalyser;

public class ControlFlowAnalyserWrapperImpl
    implements ControlFlowAnalyserWrapper {

  @Override
  public List<Block> analyze(final MethodNode methodNode) {
    return ControlFlowAnalyser.analyze(methodNode);
  }
}
