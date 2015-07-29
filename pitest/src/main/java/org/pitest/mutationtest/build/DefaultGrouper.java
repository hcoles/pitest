package org.pitest.mutationtest.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.pitest.classinfo.ClassName;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.engine.MutationDetails;

public class DefaultGrouper implements MutationGrouper {

  private final int unitSize;

  public DefaultGrouper(final int unitSize) {
    this.unitSize = unitSize;
  }

  @Override
  public List<List<MutationDetails>> groupMutations(
      final Collection<ClassName> codeClasses,
      final Collection<MutationDetails> mutations) {
    final Map<ClassName, Collection<MutationDetails>> bucketed = FCollection
        .bucket(mutations, byClass());
    final List<List<MutationDetails>> chunked = new ArrayList<List<MutationDetails>>();
    for (final Collection<MutationDetails> each : bucketed.values()) {
      shrinkToMaximumUnitSize(chunked, each);
    }

    return chunked;
  }

  private void shrinkToMaximumUnitSize(
      final List<List<MutationDetails>> chunked,
      final Collection<MutationDetails> each) {
    if (this.unitSize > 0) {
      for (final List<MutationDetails> ms : FCollection.splitToLength(
          this.unitSize, each)) {
        chunked.add(ms);
      }
    } else {
      chunked.add(new ArrayList<MutationDetails>(each));
    }
  }

  private static F<MutationDetails, ClassName> byClass() {
    return new F<MutationDetails, ClassName>() {
      @Override
      public ClassName apply(final MutationDetails a) {
        return a.getClassName();
      }
    };
  }

}
