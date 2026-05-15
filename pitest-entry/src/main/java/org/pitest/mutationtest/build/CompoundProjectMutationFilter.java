package org.pitest.mutationtest.build;

import org.pitest.classpath.CodeSource;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.List;

public class CompoundProjectMutationFilter implements ProjectMutationFilter {

  public static final ProjectMutationFilter PASSTHROUGH = mutations -> mutations;

  private final List<ProjectMutationFilter> filters;

  public CompoundProjectMutationFilter(List<ProjectMutationFilter> filters) {
    this.filters = filters;
  }

  @Override
  public void initialise(CodeSource code) {
    filters.forEach(f -> f.initialise(code));
  }

  @Override
  public Collection<MutationDetails> filter(Collection<MutationDetails> mutations) {
    Collection<MutationDetails> current = mutations;
    for (ProjectMutationFilter f : filters) {
      current = f.filter(current);
    }
    return current;
  }
}
