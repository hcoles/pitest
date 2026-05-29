package org.pitest.mutationtest.build;

import org.pitest.classpath.CodeSource;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

public class CompoundProjectMutationInterceptor implements ProjectMutationInterceptor {

  private final List<ProjectMutationInterceptor> filters = new ArrayList<>();

  public CompoundProjectMutationInterceptor(List<ProjectMutationInterceptor> filters) {
    this.filters.addAll(filters);
    this.filters.sort(comparing(ProjectMutationInterceptor::type));
  }

  public static ProjectMutationInterceptor passThrough() {
    return new CompoundProjectMutationInterceptor(List.of());
  }

  @Override
  public void initialise(CodeSource code) {
    filters.forEach(f -> f.initialise(code));
  }

  @Override
  public Collection<MutationDetails> intercept(Collection<MutationDetails> mutations) {
    Collection<MutationDetails> current = mutations;
    for (ProjectMutationInterceptor f : filters) {
      current = f.intercept(current);
    }
    return current;
  }

  @Override
  public InterceptorType type() {
    return InterceptorType.OTHER;
  }

  public CompoundProjectMutationInterceptor filter(Predicate<InterceptorType> p) {
    return new CompoundProjectMutationInterceptor(filters.stream()
        .filter(f -> p.test(f.type()))
            .collect(Collectors.toList()));
  }
}
