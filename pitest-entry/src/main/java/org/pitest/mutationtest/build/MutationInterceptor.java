package org.pitest.mutationtest.build;

import java.util.Collection;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

public interface MutationInterceptor {

  InterceptorType type();

  void begin(ClassTree clazz);

  Collection<MutationDetails> intercept(Collection<MutationDetails> mutations, Mutater m);

  void end();

}
