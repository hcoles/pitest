package org.pitest.mutationtest.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

public class CompoundMutationInterceptor implements MutationInterceptor {

  private final List<MutationInterceptor> children = new ArrayList<MutationInterceptor>();

  public CompoundMutationInterceptor(List<MutationInterceptor> interceptors) {
    this.children.addAll(interceptors);
  }

  public static MutationInterceptor nullInterceptor() {
    return new CompoundMutationInterceptor(Collections.<MutationInterceptor>emptyList());
  }
  
  @Override
  public void begin(ClassName clazz) {
    for (final MutationInterceptor each : this.children) {
      each.begin(clazz);
    }
  }

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    Collection<MutationDetails> modified = mutations;
    for (final MutationInterceptor each : this.children) {
      modified = each.intercept(mutations, m);
    }
    return modified;
  }

  @Override
  public void end() {
    for (final MutationInterceptor each : this.children) {
      each.end();
    }
  }

}
