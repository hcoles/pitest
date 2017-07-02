package org.pitest.mutationtest.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

public class CompoundMutationInterceptor implements MutationInterceptor {

  private final List<MutationInterceptor> children = new ArrayList<MutationInterceptor>();

  public CompoundMutationInterceptor(List<MutationInterceptor> interceptors) {
    this.children.addAll(interceptors);
    Collections.sort(children, sortByType());
  }

  public static MutationInterceptor nullInterceptor() {
    return new CompoundMutationInterceptor(Collections.<MutationInterceptor>emptyList());
  }
  
  @Override
  public void begin(ClassTree clazz) {
    for (final MutationInterceptor each : this.children) {
      each.begin(clazz);
    }
  }

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    Collection<MutationDetails> modified = mutations;
    for (final MutationInterceptor each : this.children) {
      modified = each.intercept(modified, m);
    }
    return modified;
  }

  @Override
  public void end() {
    for (final MutationInterceptor each : this.children) {
      each.end();
    }
  }

  @Override
  public InterceptorType type() {
    return InterceptorType.OTHER;
  }
  
  private static Comparator<? super MutationInterceptor> sortByType() {
    return new Comparator<MutationInterceptor>() {
      @Override
      public int compare(MutationInterceptor o1, MutationInterceptor o2) {
        return o1.type().compareTo(o2.type());
      }
      
    };
  }

}
