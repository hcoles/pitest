package org.pitest.mutationtest.incremental;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pitest.functional.Option;
import org.pitest.mutationtest.MutationAnalyser;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.execute.MutationStatusTestPair;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.mutationtest.results.MutationResult;

public class IncrementalAnalyser implements MutationAnalyser {

  private final CodeHistory history;

  public IncrementalAnalyser(final CodeHistory history) {
    this.history = history;
  }

  public Collection<MutationResult> analyse(
      final Collection<MutationDetails> mutation) {

    final List<MutationResult> mrs = new ArrayList<MutationResult>(
        mutation.size());
    for (final MutationDetails each : mutation) {
      final Option<MutationStatusTestPair> maybeResult = this.history
          .getPreviousResult(each.getId());
      if (maybeResult.hasNone()) {
        mrs.add(analyseFromScratch(each));
      } else {
        mrs.add(analyseFromHistory(each, maybeResult.value()));
      }
    }

    return mrs;

  }

  private MutationResult analyseFromHistory(final MutationDetails each,
      final MutationStatusTestPair mutationStatusTestPair) {

    if (!this.history.hasClassChanged(each.getClassName())
        && (mutationStatusTestPair.getStatus() == DetectionStatus.TIMED_OUT)) {
      return makeResult(each, DetectionStatus.TIMED_OUT);
    }
    return analyseFromScratch(each);
  }

  private MutationResult analyseFromScratch(final MutationDetails mutation) {
    return makeResult(mutation, DetectionStatus.NOT_STARTED);
  }

  private MutationResult makeResult(final MutationDetails each,
      final DetectionStatus status) {
    return new MutationResult(each, new MutationStatusTestPair(0, status));
  }

}
