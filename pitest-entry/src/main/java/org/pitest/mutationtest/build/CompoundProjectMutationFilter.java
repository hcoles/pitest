package org.pitest.mutationtest.build;

import org.pitest.classpath.CodeSource;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CompoundProjectMutationFilter implements ProjectMutationFilter {

  private final List<ProjectMutationFilter> filters;

  public CompoundProjectMutationFilter(List<ProjectMutationFilter> filters) {
    this.filters = filters;
  }

  public static ProjectMutationFilter passThrough() {
    return new CompoundProjectMutationFilter(List.of());
  }

  @Override
  public void initialise(CodeSource code) {
    filters.forEach(f -> f.initialise(code));
  }

  @Override
  public Collection<MutationDetails> intercept(Collection<MutationDetails> mutations) {
    Collection<MutationDetails> current = mutations;
    for (ProjectMutationFilter f : filters) {
      current = f.intercept(current);
    }
    return current;
  }

  @Override
  public InterceptorType type() {
    return InterceptorType.OTHER;
  }

  public CompoundProjectMutationFilter filter(Predicate<InterceptorType> p) {
    return new CompoundProjectMutationFilter(filters.stream()
        .filter(f -> p.test(f.type()))
            .collect(Collectors.toList()));
  }
}
