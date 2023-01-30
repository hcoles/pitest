package org.pitest.mutationtest.build;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classpath.CodeSource;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

public class CompoundMutationInterceptor implements MutationInterceptor {

  private final List<MutationInterceptor> children = new ArrayList<>();

  public CompoundMutationInterceptor(List<? extends MutationInterceptor> interceptors) {
    this.children.addAll(interceptors);
    this.children.sort(comparing(MutationInterceptor::type));
  }

  public static MutationInterceptor nullInterceptor() {
    return new CompoundMutationInterceptor(Collections.emptyList());
  }

  public CompoundMutationInterceptor filter(Predicate<MutationInterceptor> p) {
    return new CompoundMutationInterceptor(children.stream()
            .filter(p)
            .collect(Collectors.toList()));
  }

  @Override
  public void initialise(CodeSource code) {
    this.children.forEach(each -> each.initialise(code));
  }

  @Override
  public void begin(ClassTree clazz) {
    this.children.forEach(each -> each.begin(clazz));
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
    this.children.forEach(MutationInterceptor::end);
  }

  @Override
  public InterceptorType type() {
    return InterceptorType.OTHER;
  }

}
