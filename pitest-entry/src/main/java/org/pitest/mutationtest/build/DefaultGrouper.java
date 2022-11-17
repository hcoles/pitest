package org.pitest.mutationtest.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.pitest.classinfo.ClassName;
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
        .bucket(mutations, MutationDetails::getClassName);
    final List<List<MutationDetails>> chunked = new ArrayList<>();
    for (final Map.Entry<ClassName,Collection<MutationDetails>> each : bucketed.entrySet()) {
      shrinkToMaximumUnitSize(chunked, each.getValue());
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
      chunked.add(new ArrayList<>(each));
    }
  }

}
