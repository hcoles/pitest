package org.pitest.mutationtest.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;

public class CompoundInterceptorFactory implements MutationInterceptorFactory {

  private final List<MutationInterceptorFactory> children = new ArrayList<MutationInterceptorFactory>();

  public CompoundInterceptorFactory(
      Collection<? extends MutationInterceptorFactory> filters) {
    this.children.addAll(filters);
  }

  @Override
  public String description() {
    return null;
  }

  @Override
  public MutationInterceptor createInterceptor(Properties props, ClassByteArraySource source) {
    List<MutationInterceptor> interceptors = FCollection.map(this.children,
        toInterceptor(props, source));
    return new CompoundMutationInterceptor(interceptors);
  }

  private static F<MutationInterceptorFactory, MutationInterceptor> toInterceptor(
      final Properties props, final ClassByteArraySource source) {
    return new F<MutationInterceptorFactory, MutationInterceptor>() {
      @Override
      public MutationInterceptor apply(MutationInterceptorFactory a) {
        return a.createInterceptor(props, source);
      }

    };
  }

}