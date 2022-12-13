package org.pitest.mutationtest.tdghistory;
import java.util.List;
import org.pitest.mutationtest.MutationAnalyser;
import org.pitest.util.Log;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.tdg.Tdg;
import java.util.EnumMap;
import java.util.logging.Logger;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import org.pitest.coverage.TestInfo;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Collections;
import org.pitest.util.CheckSumUtil;
public class TdgAnalyser implements MutationAnalyser {
    private static final Logger              LOG         = Log.getLogger();
    TdgCodeHistory tdgCodeHistory;
    Tdg tdg;
    private final Map<DetectionStatus, Long> preAnalysed = new EnumMap<>(DetectionStatus.class);
    public TdgAnalyser(TdgCodeHistory tdgCodeHistory, Tdg tdg) {
        this.tdgCodeHistory = tdgCodeHistory;
        this.tdg = tdg;
    }

    @Override
    public Collection<MutationResult> analyse(
      Collection<MutationDetails> mutationsForClasses) {
        final List<MutationResult> mrs = new ArrayList<>(mutationsForClasses.size());
        for (final MutationDetails each : mutationsForClasses) {
            if (this.tdgCodeHistory.hasClassChanged(each.getClassName())) {
                mrs.add(analyseFromScratch(each));
                continue;
            }
            final Optional<MutationStatusTestPair> maybeResult = this.tdgCodeHistory
                .getPreviousResult(each.getId());
            if (maybeResult.isPresent()) {
                mrs.add(analyseFromHistory(each, maybeResult.get()));
            } else {
                mrs.add(analyseFromScratch(each));
                continue;
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
    
        LOG.info("Tdg analysis reduced number of mutations by " + numberOfReducedMutations );
      }

    private MutationResult analyseFromHistory(final MutationDetails each,
      final MutationStatusTestPair mutationStatusTestPair) {
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
      }
    }

    if ((mutationStatusTestPair.getStatus() == DetectionStatus.SURVIVED)
        && !this.tdgCodeHistory.hasTestsChanged(each.getClassName())) {
      return makeResult(each, DetectionStatus.SURVIVED);
    }
    return analyseFromScratch(each);
  }

    private List<String> filterUnchangedKillingTests(final MutationDetails each,
    final MutationStatusTestPair mutationStatusTestPair ) {
        return this.tdg.getTests(each.getClassName()).stream().filter(isAKillingTestFor(mutationStatusTestPair))
        .filter(testClassDidNotChange())
        .map(TestInfo::getName)
            .collect(Collectors.toList());
    }
    private Predicate<TestInfo> testClassDidNotChange() {
        return a -> !this.tdgCodeHistory.hasClassChanged(TestInfo.toDefiningClassName().apply(a));
    }
    private static Predicate<TestInfo> isAKillingTestFor(final MutationStatusTestPair mutation) {
        final List<String> killingTestNames = mutation.getKillingTests();
        return a -> killingTestNames.contains(a.getName());
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
        this.preAnalysed.merge(status, 1L, Long::sum);
    }
}
