package org.pitest.mutationtest.incremental;

import org.pitest.classinfo.ClassName;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.TestInfo;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationAnalyser;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.util.Log;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class IncrementalAnalyser implements MutationAnalyser {

  private final static Logger        LOG         = Log.getLogger();

  private CodeHistory                history;
  private CoverageDatabase           coverage;
  private Map<DetectionStatus, Long> preAnalysed = createStatusMap();

  public IncrementalAnalyser(CodeHistory history, CoverageDatabase coverage) {
    this.history = history;
    this.coverage = coverage;
  }

  private static Map<DetectionStatus, Long> createStatusMap() {
    Map<DetectionStatus, Long> map = new HashMap<DetectionStatus, Long>();
    for (DetectionStatus detectionStatus : DetectionStatus.values()) {
      map.put(detectionStatus, 0L);
    }
    return map;
  }

  public Collection<MutationResult> analyse(
      Collection<MutationDetails> mutation) {

    List<MutationResult> mutationResultSet = new ArrayList<MutationResult>(mutation.size());
    for (MutationDetails mutationDetails : mutation) {
      Option<MutationStatusTestPair> maybeResult = history.getPreviousResult(mutationDetails.getId());
      if (maybeResult.hasNone()) {
        mutationResultSet.add(analyseFromScratch(mutationDetails));
      } else {
        mutationResultSet.add(analyseFromHistory(mutationDetails, maybeResult.value()));
      }
    }

    logTotals(preAnalysed);

    return mutationResultSet;

  }

  private static void logTotals(Map<DetectionStatus, Long> preAnalysed) {
    for (Entry<DetectionStatus, Long> entry : preAnalysed.entrySet()) {
      if (entry.getValue() != 0) {
        LOG.fine("Incremental analysis set " + entry.getValue() + " mutations to a status of " + entry.getKey());
      }
    }

  }

  private MutationResult analyseFromHistory(MutationDetails each, MutationStatusTestPair mutationStatusTestPair) {

    ClassName clazz = each.getClassName();

    if (history.hasClassChanged(clazz)) {
      return analyseFromScratch(each);
    }

    DetectionStatus detectionStatus = mutationStatusTestPair.getStatus();

    if (detectionStatus == DetectionStatus.TIMED_OUT) {
      return makeResult(each, DetectionStatus.TIMED_OUT);
    }

    if ((detectionStatus == DetectionStatus.KILLED) && killingTestHasNotChanged(each, mutationStatusTestPair)) {
      return makeResult(each, DetectionStatus.KILLED, mutationStatusTestPair.getKillingTest().value());
    }

    if ((detectionStatus == DetectionStatus.SURVIVED)
            && !history.hasCoverageChanged(clazz, coverage.getCoverageIdForClass(clazz))) {
      return makeResult(each, DetectionStatus.SURVIVED);
    }

    return analyseFromScratch(each);
  }

  private boolean killingTestHasNotChanged(MutationDetails mutationDetails, MutationStatusTestPair mutationStatusTestPair) {
    Collection<TestInfo> allTests = coverage.getTestsForClass(mutationDetails.getClassName());

    List<ClassName> testClasses = FCollection.filter(allTests,
        testIsCalled(mutationStatusTestPair.getKillingTest().value())).map(
        TestInfo.toDefiningClassName());

    if (testClasses.isEmpty()) {
      return false;
    }

    return !history.hasClassChanged(testClasses.get(0));
  }

  private static F<TestInfo, Boolean> testIsCalled(final String testName) {
    return new F<TestInfo, Boolean>() {
      public Boolean apply(TestInfo a) {
        return a.getName().equals(testName);
      }
    };
  }

  private MutationResult analyseFromScratch(MutationDetails mutation) {
    return makeResult(mutation, DetectionStatus.NOT_STARTED);
  }

  private MutationResult makeResult(MutationDetails mutationDetails, DetectionStatus status) {
    return makeResult(mutationDetails, status, null);
  }

  private MutationResult makeResult(MutationDetails mutationDetails, DetectionStatus status, String killingTest) {
    updatePreanalysedTotal(status);
    return new MutationResult(mutationDetails, new MutationStatusTestPair(0, status, killingTest));
  }

  private void updatePreanalysedTotal(DetectionStatus status) {
    if (status != DetectionStatus.NOT_STARTED) {
      long count = preAnalysed.get(status);
      preAnalysed.put(status, count + 1);
    }
  }

}
