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

class IncrementalAnalyser implements MutationAnalyser {

  private static final Logger              LOG         = Log.getLogger();

  private final CodeHistory                history;
  private final CoverageDatabase           coverage;
  private final Map<DetectionStatus, Long> preAnalysed = new EnumMap<>(DetectionStatus.class);

  IncrementalAnalyser(CodeHistory history, CoverageDatabase coverage) {
    this.history = history;
    this.coverage = coverage;
  }

  @Override
  public List<MutationResult> analyse(Collection<MutationDetails> mutation) {

    final List<MutationResult> mrs = new ArrayList<>(
        mutation.size());
    for (final MutationDetails each : mutation) {
      final Optional<MutationStatusTestPair> maybeResult = this.history
          .getPreviousResult(each.getId());
      // discard the mutant if no existing result
      if (maybeResult.isPresent()) {
        mrs.add(analyseFromHistory(each, maybeResult.get()));
      }
    }

    logTotals();

    return mrs;

  }

  private void logTotals() {
    int numberOfReducedMutations = 0;
    for (final Entry<DetectionStatus, Long> each : this.preAnalysed.entrySet()) {
      final Long numberOfMutationsInStatus = each.getValue();
      final DetectionStatus mutationStatus = each.getKey();
      LOG.fine("Incremental analysis set " + numberOfMutationsInStatus
          + " mutations to a status of " + mutationStatus);
      if (mutationStatus != DetectionStatus.NOT_STARTED) {
        numberOfReducedMutations += numberOfMutationsInStatus;
      }
    }

    LOG.info("Incremental analysis reduced number of mutations by " + numberOfReducedMutations );
  }

  private MutationResult analyseFromHistory(MutationDetails each, MutationStatusTestPair mutationStatusTestPair) {

    final ClassName clazz = each.getClassName();

    if (this.history.hasClassChanged(clazz)) {
      if (mutationStatusTestPair.getKillingTest().isPresent()) {
        return prioritiseLastTest(each, mutationStatusTestPair.getKillingTest().get());
      }
      return analyseFromScratch(each);
    }

    if (mutationStatusTestPair.getStatus() == DetectionStatus.TIMED_OUT) {
      return makeResult(each, DetectionStatus.TIMED_OUT);
    }

    if ((mutationStatusTestPair.getStatus() == DetectionStatus.KILLED)) {
      final List<String> killingTestNames = filterUnchangedKillingTests(each, mutationStatusTestPair);

      if (!killingTestNames.isEmpty()) {
        return makeResult(
                each,
                DetectionStatus.KILLED,
                killingTestNames,
                mutationStatusTestPair.getSucceedingTests());
      } else {
        if (mutationStatusTestPair.getKillingTest().isPresent()) {
          return prioritiseLastTest(each, mutationStatusTestPair.getKillingTest().get());
        }
      }
    }

    if ((mutationStatusTestPair.getStatus() == DetectionStatus.SURVIVED)
        && !this.history.hasCoverageChanged(clazz,
            this.coverage.getCoverageIdForClass(clazz))) {
      return makeResult(each, DetectionStatus.SURVIVED);
    }

    return analyseFromScratch(each);
  }
  private List<String> filterUnchangedKillingTests(final MutationDetails each,
                                                   final MutationStatusTestPair mutationStatusTestPair) {

    return this.coverage.getTestsForClass(each.getClassName()).stream()
        .filter(isAKillingTestFor(mutationStatusTestPair))
        .filter(testClassDidNotChange())
        .map(TestInfo::getName)
        .collect(Collectors.toList());
  }

  private Predicate<TestInfo> testClassDidNotChange() {
    return a -> !this.history.hasClassChanged(TestInfo.toDefiningClassName().apply(a));
  }

  private static Predicate<TestInfo> isAKillingTestFor(final MutationStatusTestPair mutation) {
    final List<String> killingTestNames = mutation.getKillingTests();
    return a -> killingTestNames.contains(a.getName());
  }

  private MutationResult prioritiseLastTest(MutationDetails mutation, String killingTestName) {
    List<TestInfo> mutableOrderedTestList = mutation.getTestsInOrder();

    Optional<TestInfo> maybeKillingTest = mutation.getTestsInOrder().stream()
            .filter(ti -> ti.getName().equals(killingTestName))
            .findFirst();

    // last killing test is no longer available
    if (!maybeKillingTest.isPresent()) {
      return analyseFromScratch(mutation);
    }

    // hack the ordered list to put the killing test at the front
    mutableOrderedTestList.remove(maybeKillingTest.get());
    mutableOrderedTestList.add(0, maybeKillingTest.get());

    return analyseFromScratch(mutation);
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
        killingTests, succeedingTests, each.getTestsInOrder().stream()
            .map(TestInfo::getName).collect(Collectors.toList())));
  }

  private void updatePreanalysedTotal(final DetectionStatus status) {
      this.preAnalysed.merge(status, 1L, Long::sum);
  }

}
