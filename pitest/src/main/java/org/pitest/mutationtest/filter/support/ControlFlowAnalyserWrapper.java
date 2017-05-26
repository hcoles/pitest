package org.pitest.mutationtest.filter.support;

import java.util.List;

import org.objectweb.asm.tree.MethodNode;
import org.pitest.coverage.analysis.Block;

public interface ControlFlowAnalyserWrapper {

  List<Block> analyze(MethodNode methodNode);
}
