package org.pitest.mutationtest.build.intercept.staticinitializers;

import java.util.Collection;
import java.util.function.Predicate;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.functional.FCollection;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

class StaticInitializerFilter implements MutationInterceptor {

  @Override
  public void begin(ClassTree clazz) {
    // noop
  }

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    return FCollection.filter(mutations, Prelude.not(isInStaticInitCode()));
  }

  private Predicate<MutationDetails> isInStaticInitCode() {
    return a -> a.isInStaticInitializer();
  }

  @Override
  public void end() {
    // noop
  }

  @Override
  public InterceptorType type() {
    return InterceptorType.FILTER;
  }

}
