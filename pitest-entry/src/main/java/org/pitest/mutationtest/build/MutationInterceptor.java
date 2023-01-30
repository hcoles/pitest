package org.pitest.mutationtest.build;

import java.util.Collection;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classpath.CodeSource;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

public interface MutationInterceptor {

  InterceptorType type();

  /**
   * Called once per instance prior to intercepting
   * @param code Current code source
   */
  default void initialise(CodeSource code) {
    // noop
  }

  void begin(ClassTree clazz);

  Collection<MutationDetails> intercept(Collection<MutationDetails> mutations, Mutater m);

  void end();

}
