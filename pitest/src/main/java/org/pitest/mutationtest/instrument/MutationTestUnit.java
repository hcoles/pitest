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
package org.pitest.mutationtest.instrument;

import static org.pitest.functional.Prelude.printWith;
import static org.pitest.util.Unchecked.translateCheckedException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.pitest.Description;
import org.pitest.MetaData;
import org.pitest.classinfo.ClassName;
import org.pitest.extension.Configuration;
import org.pitest.extension.ResultCollector;
import org.pitest.functional.Prelude;
import org.pitest.functional.SideEffect1;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.execute.MutationTestProcess;
import org.pitest.mutationtest.execute.SlaveArguments;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.testunit.AbstractTestUnit;
import org.pitest.util.ExitCode;
import org.pitest.util.JavaAgent;
import org.pitest.util.Log;
import org.pitest.util.ProcessArgs;
import org.pitest.util.SocketFinder;

public class MutationTestUnit extends AbstractTestUnit {

  private final static Logger               LOG = Log.getLogger();

  private final JavaAgent                   javaAgentFinder;
  private final MutationConfig              config;
  private final TimeoutLengthStrategy       timeoutStrategy;
  private final Collection<MutationDetails> availableMutations;
  private final boolean                     verbose;
  private final String                      classPath;
  private final File                        baseDir;
  private final Configuration               pitConfig;

  private final Collection<ClassName>       testClasses;

  public MutationTestUnit(final File baseDir,
      final Collection<MutationDetails> availableMutations,
      final Collection<ClassName> testClasses, final Configuration pitConfig,
      final MutationConfig mutationConfig, final JavaAgent javaAgentFinder,
      final TimeoutLengthStrategy timeoutStrategy, final boolean verbose,
      final String classPath) {
    super(new Description("Mutation test"));
    this.availableMutations = availableMutations;
    this.config = mutationConfig;
    this.pitConfig = pitConfig;
    this.javaAgentFinder = javaAgentFinder;
    this.timeoutStrategy = timeoutStrategy;
    this.testClasses = testClasses;
    this.verbose = verbose;
    this.classPath = classPath;
    this.baseDir = baseDir;
  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {

    try {
      rc.notifyStart(this.getDescription());
      runTests(rc);
    } catch (final Throwable ex) {
      rc.notifyEnd(this.getDescription(), ex);
    }

  }

  private void runTests(final ResultCollector rc) {

    try {
      if (!this.availableMutations.isEmpty()) {
        runTestsForMutations(rc);
      } else {
        LOG.fine("No mutations to detect");
        rc.notifySkipped(this.getDescription());
      }
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

  private void runTestsForMutations(final ResultCollector rc)
      throws IOException, InterruptedException {

    final MutationStatusMap mutations = new MutationStatusMap();

    mutations.setStatusForMutations(this.availableMutations,
        DetectionStatus.NOT_STARTED);

    mutations.markUncoveredMutations();

    runTestsInSeperateProcess(mutations);

    reportResults(mutations, rc);
  }

  private void runTestInSeperateProcessForMutationRange(
      final MutationStatusMap mutations) throws IOException,
      InterruptedException {

    final Collection<MutationDetails> remainingMutations = mutations
        .getUnrunMutations();
    final MutationTestProcess worker = createWorker(remainingMutations);
    worker.start();

    setFirstMutationToStatusOfStartedInCaseSlaveFailsAtBoot(mutations,
        remainingMutations);

    final ExitCode exitCode = waitForSlaveToDie(worker);
    worker.results(mutations);

    correctResultForProcessExitCode(mutations, exitCode);

  }

  private MutationTestProcess createWorker(
      final Collection<MutationDetails> remainingMutations) {
    final SlaveArguments fileArgs = new SlaveArguments(remainingMutations,
        this.testClasses, this.config.getEngine(), this.timeoutStrategy,
        Log.isVerbose(), this.pitConfig);

    final ProcessArgs args = ProcessArgs.withClassPath(this.classPath)
        .andJVMArgs(getJVMArgs()).andJavaAgentFinder(this.javaAgentFinder)
        .andBaseDir(this.baseDir).andStdout(captureStdOutIfVerbose())
        .andStderr(printWith("stderr "));

    final SocketFinder sf = new SocketFinder();
    final MutationTestProcess worker = new MutationTestProcess(
        sf.getNextAvailableServerSocket(), args, fileArgs);
    return worker;
  }

  private ExitCode waitForSlaveToDie(final MutationTestProcess worker) {
    final ExitCode exitCode = worker.waitToDie();
    LOG.fine("Exit code was - " + exitCode);
    return exitCode;
  }

  private SideEffect1<String> captureStdOutIfVerbose() {
    if (this.verbose) {
      return Prelude.printWith("stdout ");
    } else {
      return Prelude.noSideEffect(String.class);
    }

  }

  private void setFirstMutationToStatusOfStartedInCaseSlaveFailsAtBoot(
      final MutationStatusMap mutations,
      final Collection<MutationDetails> remainingMutations) {
    mutations.setStatusForMutation(remainingMutations.iterator().next(),
        DetectionStatus.STARTED);
  }

  private void correctResultForProcessExitCode(
      final MutationStatusMap mutations, final ExitCode exitCode) {

    if (!exitCode.isOk()) {
      final Collection<MutationDetails> unfinishedRuns = mutations
          .getUnfinishedRuns();
      final DetectionStatus status = DetectionStatus
          .getForErrorExitCode(exitCode);
      LOG.warning("Slave exited abnormally due to " + status);
      LOG.fine("Setting " + unfinishedRuns.size() + " unfinished runs to "
          + status + " state");
      mutations.setStatusForMutations(unfinishedRuns, status);

    } else {
      LOG.fine("Slave exited ok");
    }

  }

  private List<String> getJVMArgs() {
    return this.config.getJVMArgs();
  }

  private void runTestsInSeperateProcess(final MutationStatusMap mutations)
      throws IOException, InterruptedException {

    while (mutations.hasUnrunMutations()) {
      runTestInSeperateProcessForMutationRange(mutations);
    }

  }

  private void reportResults(final MutationStatusMap mutationsMap,
      final ResultCollector rc) {

    final MetaData md = new MutationMetaData(this.config.getMutatorNames(),
        mutationsMap.createMutationResults());

    rc.notifyEnd(this.getDescription(), md);

  }

  public MutationConfig getMutationConfig() {
    return this.config;
  }

}
