package org.pitest.mutationtest.build;

import java.util.Collection;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

public interface MutationInterceptor {
  
  void begin(ClassName clazz);
  
  Collection<MutationDetails> intercept(Collection<MutationDetails> mutations, Mutater m);
  
  void end();

}
