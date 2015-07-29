package org.pitest.mutationtest.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pitest.mutationtest.engine.MutationDetails;

class CompoundMutationFilter implements MutationFilter {

  private final List<MutationFilter> children = new ArrayList<MutationFilter>();

  CompoundMutationFilter(List<MutationFilter> children) {
    this.children.addAll(children);
  }

  @Override
  public Collection<MutationDetails> filter(
      Collection<MutationDetails> mutations) {
    Collection<MutationDetails> modified = mutations;
    for (MutationFilter each : this.children) {
      modified = each.filter(modified);
    }
    return modified;
  }

}
