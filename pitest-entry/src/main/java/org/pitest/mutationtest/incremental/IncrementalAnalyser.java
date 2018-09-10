package org.pitest.mutationtest.incremental;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.pitest.classinfo.ClassName;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.TestInfo;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationAnalyser;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.util.Log;

public class IncrementalAnalyser implements MutationAnalyser {

  private static final Logger              LOG         = Log.getLogger();

  private final CodeHistory                history;
  private final CoverageDatabase           coverage;
  private final Map<DetectionStatus, Long> preAnalysed = createStatusMap();

  public IncrementalAnalyser(final CodeHistory history,
      final CoverageDatabase coverage) {
    this.history = history;
    this.coverage = coverage;
  }

  private static Map<DetectionStatus, Long> createStatusMap() {
    final EnumMap<DetectionStatus, Long> map = new EnumMap<>(DetectionStatus.class);
    for (final DetectionStatus each : DetectionStatus.values()) {
      map.put(each, 0L);
    }
    return map;
  }

  @Override
  public Collection<MutationResult> analyse(
      final Collection<MutationDetails> mutation) {

    final List<MutationResult> mrs = new ArrayList<>(
        mutation.size());
    for (final MutationDetails each : mutation) {
      final Optional<MutationStatusTestPair> maybeResult = this.history
          .getPreviousResult(each.getId());
      if (!maybeResult.isPresent()) {
        mrs.add(analyseFromScratch(each));
      } else {
        mrs.add(analyseFromHistory(each, maybeResult.get()));
      }
    }

    logTotals();

    return mrs;

  }

  private void logTotals() {
    for (final Entry<DetectionStatus, Long> each : this.preAnalysed.entrySet()) {
      if (each.getValue() != 0) {
        LOG.fine("Incremental analysis set " + each.getValue()
            + " mutations to a status of " + each.getKey());
      }
    }

  }

  private MutationResult analyseFromHistory(final MutationDetails each,
      final MutationStatusTestPair mutationStatusTestPair) {

    final ClassName clazz = each.getClassName();

    if (this.history.hasClassChanged(clazz)) {
      return analyseFromScratch(each);
    }

    if (mutationStatusTestPair.getStatus() == DetectionStatus.TIMED_OUT) {
      return makeResult(each, DetectionStatus.TIMED_OUT);
    }

    if ((mutationStatusTestPair.getStatus() == DetectionStatus.KILLED)
        && killingTestHasNotChanged(each, mutationStatusTestPair)) {
      return makeResult(each, DetectionStatus.KILLED, mutationStatusTestPair
          .getKillingTests(), mutationStatusTestPair.getSucceedingTests());
    }

    if ((mutationStatusTestPair.getStatus() == DetectionStatus.SURVIVED)
        && !this.history.hasCoverageChanged(clazz,
            this.coverage.getCoverageIdForClass(clazz))) {
      return makeResult(each, DetectionStatus.SURVIVED);
    }

    return analyseFromScratch(each);
  }

  private boolean killingTestHasNotChanged(final MutationDetails each,
      final MutationStatusTestPair mutationStatusTestPair) {
    final Collection<TestInfo> allTests = this.coverage.getTestsForClass(each
        .getClassName());

    final List<ClassName> testClasses = allTests.stream()
        .filter(testIsCalled(mutationStatusTestPair.getKillingTest().get()))
        .map(TestInfo.toDefiningClassName())
        .collect(Collectors.toList());

    if (testClasses.isEmpty()) {
      return false;
    }

    return !this.history.hasClassChanged(testClasses.get(0));

  }

  private static Predicate<TestInfo> testIsCalled(final String testName) {
    return a -> a.getName().equals(testName);
  }

  private MutationResult analyseFromScratch(final MutationDetails mutation) {
    return makeResult(mutation, DetectionStatus.NOT_STARTED);
  }

  private MutationResult makeResult(final MutationDetails each,
      final DetectionStatus status) {
    return makeResult(each, status, Collections.emptyList(), Collections.emptyList());
  }

  private MutationResult makeResult(final MutationDetails each,
      final DetectionStatus status, final List<String> killingTests,
      final List<String> succeedingTests) {
    updatePreanalysedTotal(status);
    return new MutationResult(each, new MutationStatusTestPair(0, status,
        killingTests, succeedingTests));
  }

  private void updatePreanalysedTotal(final DetectionStatus status) {
    if (status != DetectionStatus.NOT_STARTED) {
      final long count = this.preAnalysed.get(status);
      this.preAnalysed.put(status, count + 1);
    }
  }

}
