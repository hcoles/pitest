package org.pitest.mutationtest.build.intercept.timeout;

import java.util.Collection;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.Slot;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

public class SimpleInfiniteLoopInterceptor implements MutationInterceptor {

  private Slot<AbstractInsnNode> loopStart       = new Slot<AbstractInsnNode>();
  private Slot<Integer>          counterVariable = new Slot<Integer>();

  @Override
  public InterceptorType type() {
    return InterceptorType.FILTER;
  }

  @Override
  public void begin(ClassTree clazz) {
    // TODO Auto-generated method stub

  }

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void end() {
    // TODO Auto-generated method stub

  }

}
