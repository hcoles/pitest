package org.pitest.mutationtest.build.intercept.javafeatures;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

public class TryWithResourcesFilter implements MutationInterceptor {

  private Set<Integer> lines;
  
  @Override
  public InterceptorType type() {
    return InterceptorType.FILTER;
  }

  @Override
  public void begin(ClassTree clazz) {
    lines = new HashSet<Integer>();
    for (MethodTree each : clazz.methods()) {
      checkMehod(each,lines);
    }
  }

  private void checkMehod(MethodTree each, Set<Integer> lines) {
    each.rawNode().accept(new TryWithResourcesMethodVisitor(lines)); 
  }

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    return FCollection.filter(mutations, Prelude.not(isOnMarkedLine()));
  }
  
  private F<MutationDetails, Boolean> isOnMarkedLine() {
    return new  F<MutationDetails, Boolean>() {
      @Override
      public Boolean apply(MutationDetails a) {
        return lines.contains(a.getClassLine().getLineNumber());
      }  
    };
  }

  @Override
  public void end() {
    
  }

}
