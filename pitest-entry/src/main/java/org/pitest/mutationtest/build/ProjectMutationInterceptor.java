package org.pitest.mutationtest.build;

import java.util.Collection;

import org.pitest.classpath.CodeSource;
import org.pitest.mutationtest.engine.MutationDetails;

public interface ProjectMutationInterceptor {

  /**
   * Called once prior to filtering
   * @param code Current code source
   */
  default void initialise(CodeSource code) {
    // noop
  }

  Collection<MutationDetails> intercept(Collection<MutationDetails> mutations);

  InterceptorType type();

}
