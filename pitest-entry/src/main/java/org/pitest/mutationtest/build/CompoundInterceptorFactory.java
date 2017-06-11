package org.pitest.mutationtest.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.config.ReportOptions;

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
  public MutationInterceptor createInterceptor(ReportOptions data,
      ClassByteArraySource source) {
    List<MutationInterceptor> interceptors = FCollection.map(this.children,
        toInterceptor(data, source));
    return new CompoundMutationInterceptor(interceptors);
  }

  private static F<MutationInterceptorFactory, MutationInterceptor> toInterceptor(
      final ReportOptions data, final ClassByteArraySource source) {
    return new F<MutationInterceptorFactory, MutationInterceptor>() {
      @Override
      public MutationInterceptor apply(MutationInterceptorFactory a) {
        return a.createInterceptor(data, source);
      }

    };
  }

}