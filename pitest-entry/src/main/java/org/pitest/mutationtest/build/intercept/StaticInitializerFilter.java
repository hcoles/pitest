package org.pitest.mutationtest.build.intercept;

import java.util.Collection;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.build.ClassTree;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

public class StaticInitializerFilter implements MutationInterceptor {

  private final StaticInitializerInterceptor analyser;
  
  public StaticInitializerFilter(StaticInitializerInterceptor analyser) {
    this.analyser = analyser;
  }

  @Override
  public void begin(ClassTree clazz) {
    analyser.begin(clazz);
  }

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    Collection<MutationDetails> results = analyser.intercept(mutations, m);
    return FCollection.filter(results, Prelude.not(isInStaticInitCode()));
  }

  private F<MutationDetails, Boolean> isInStaticInitCode() {
    return new F<MutationDetails, Boolean>() {
      @Override
      public Boolean apply(MutationDetails a) {
        return a.isInStaticInitializer();
      }
    };
  }

  @Override
  public void end() {
    analyser.end();
  }

}
