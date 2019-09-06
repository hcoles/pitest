/*
 * Copyright 2010 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.build;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

import org.pitest.classinfo.ClassName;
import org.pitest.coverage.TestInfo;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.MutationStatusMap;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.execute.MutationTestProcess;
import org.pitest.util.ExitCode;
import org.pitest.util.Log;

public class MutationTestUnit implements MutationAnalysisUnit {

  private static final Logger               LOG = Log.getLogger();

  private final Collection<MutationDetails> availableMutations;
  private final WorkerFactory               workerFactory;

  private final Collection<ClassName>       testClasses;

  public MutationTestUnit(final Collection<MutationDetails> availableMutations,
      final Collection<ClassName> testClasses, final WorkerFactory workerFactor) {
    this.availableMutations = availableMutations;
    this.testClasses = testClasses;
    this.workerFactory = workerFactor;
  }

  @Override
  public MutationMetaData call() throws Exception {
    final MutationStatusMap mutations = new MutationStatusMap();

    mutations.setStatusForMutations(this.availableMutations,
        DetectionStatus.NOT_STARTED);

    mutations.markUncoveredMutations();

    runTestsInSeperateProcess(mutations);

    return reportResults(mutations);
  }

  @Override
  public int priority() {
    return this.availableMutations.size();
  }

  private void runTestsInSeperateProcess(final MutationStatusMap mutations)
      throws IOException, InterruptedException {
    while (mutations.hasUnrunMutations()) {
      runTestInSeperateProcessForMutationRange(mutations);
    }
  }

  private void runTestInSeperateProcessForMutationRange(
      final MutationStatusMap mutations) throws IOException,
      InterruptedException {

    final Collection<MutationDetails> remainingMutations = mutations
        .getUnrunMutations();

    //First run mutants normally
    MutationTestProcess worker = this.workerFactory.createWorker(
        remainingMutations, this.testClasses);
    worker.start();

    setFirstMutationToStatusOfStartedInCaseMinionFailsAtBoot(mutations,
        remainingMutations);

    ExitCode exitCode = waitForMinionToDie(worker);
    worker.results(mutations);

    correctResultForProcessExitCode(mutations, exitCode);

    //rerun crashing mutants with isolation
    if (this.workerFactory.isFullMutationMatrix() && !exitCode.isOk()) {
        Collection<MutationDetails> crashedRuns = mutations.getCrashed();
        //not rerun crashed from previous range
        crashedRuns.retainAll(remainingMutations);

        LOG.info("Rerunning " + crashedRuns.size() + " mutant(s) because of minion crash");
        for (MutationDetails d : crashedRuns) {
            MutationStatusTestPair result = null;
            for (TestInfo t : d.getTestsInOrder()) {
                MutationDetails singleTest = new MutationDetails(new MutationIdentifier(d.getId().getLocation(),
                        d.getId().getIndexes(),d.getMutator()), d.getFilename(), d.getDescription(),
                        d.getLineNumber(), d.getBlock());
                singleTest.addTestsInOrder(Collections.singleton(t));
                worker = this.workerFactory.createWorker(Collections.singleton(singleTest), this.testClasses);
                worker.start();
                exitCode = waitForMinionToDie(worker);
                MutationStatusTestPair r = worker.results(singleTest);

                if (exitCode != ExitCode.OK) {
                    r.setErrorStatusAndName(DetectionStatus.getForErrorExitCode(exitCode), t.getName());
                }

                if (result == null) {
                    result = r;
                } else {
                    result.accumulate(r, t.getName());
                }
            }

            if (result != null) {
                mutations.setStatusForMutation(d, result);
            }
        }
    }
  }

  private static ExitCode waitForMinionToDie(final MutationTestProcess worker) {
    final ExitCode exitCode = worker.waitToDie();
    LOG.fine("Exit code was - " + exitCode);
    return exitCode;
  }

  private static void setFirstMutationToStatusOfStartedInCaseMinionFailsAtBoot(
      final MutationStatusMap mutations,
      final Collection<MutationDetails> remainingMutations) {
    mutations.setStatusForMutation(remainingMutations.iterator().next(),
        DetectionStatus.STARTED);
  }

  private static void correctResultForProcessExitCode(
      final MutationStatusMap mutations, final ExitCode exitCode) {

    if (!exitCode.isOk()) {
      final Collection<MutationDetails> unfinishedRuns = mutations
          .getUnfinishedRuns();
      final DetectionStatus status = DetectionStatus
          .getForErrorExitCode(exitCode);
      LOG.warning("Minion exited abnormally due to " + status);
      LOG.fine("Setting " + unfinishedRuns.size() + " unfinished runs to "
          + status + " state");
      mutations.setStatusForMutations(unfinishedRuns, status);

    } else {
      LOG.fine("Minion exited ok");
    }

  }

  private static MutationMetaData reportResults(final MutationStatusMap mutationsMap) {
    return new MutationMetaData(mutationsMap.createMutationResults());
  }
}