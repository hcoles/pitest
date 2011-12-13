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
import static org.pitest.functional.Prelude.putToMap;
import static org.pitest.util.Unchecked.translateCheckedException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.pitest.Description;
import org.pitest.MetaData;
import org.pitest.classinfo.ClassName;
import org.pitest.extension.Configuration;
import org.pitest.extension.ResultCollector;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Option;
import org.pitest.functional.Prelude;
import org.pitest.functional.SideEffect1;
import org.pitest.internal.ClassPath;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.classloader.PITClassLoader;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.execute.MutationStatusTestPair;
import org.pitest.mutationtest.execute.MutationTestProcess;
import org.pitest.mutationtest.execute.SlaveArguments;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.mutationtest.results.MutationResult;
import org.pitest.testunit.AbstractTestUnit;
import org.pitest.util.ExitCode;
import org.pitest.util.JavaAgent;
import org.pitest.util.Log;
import org.pitest.util.PortFinder;
import org.pitest.util.ProcessArgs;
import org.pitest.util.Unchecked;

public class MutationTestUnit extends AbstractTestUnit {

  private final static Logger               LOG = Log.getLogger();

  private final JavaAgent                   javaAgentFinder;
  private final MutationConfig              config;
  private final TimeoutLengthStrategy       timeoutStrategy;
  private final Collection<MutationDetails> availableMutations;
  private final boolean                     verbose;

  protected final Configuration             pitConfig;

  protected final Collection<ClassName>     testClasses;

  public MutationTestUnit(final Collection<MutationDetails> availableMutations,
      final Collection<ClassName> testClasses, final Configuration pitConfig,
      final MutationConfig mutationConfig, final Description description,
      final JavaAgent javaAgentFinder,
      final TimeoutLengthStrategy timeoutStrategy, final boolean verbose) {
    super(description);
    this.availableMutations = availableMutations;
    this.config = mutationConfig;
    this.pitConfig = pitConfig;
    this.javaAgentFinder = javaAgentFinder;
    this.timeoutStrategy = timeoutStrategy;
    this.testClasses = testClasses;
    this.verbose = verbose;

  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {
    try {

      rc.notifyStart(this.getDescription());
      runTests(rc, loader);
    } catch (final Throwable ex) {
      rc.notifyEnd(this.getDescription(), ex);
    }

  }

  private void runTests(final ResultCollector rc, final ClassLoader loader) {

    try {
      if (!this.availableMutations.isEmpty()) {
        runTestsForMutations(rc, loader);
      } else {
        LOG.info("Skipping test " + this.getDescription()
            + " as no mutations found");
        rc.notifySkipped(this.getDescription());
      }
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

  private void runTestsForMutations(final ResultCollector rc,
      final ClassLoader loader) throws IOException, InterruptedException {
    final String cp = createClassPath(loader);

    final Map<MutationDetails, MutationStatusTestPair> mutations = new HashMap<MutationDetails, MutationStatusTestPair>();
    if (hasTestCoverage(this.testClasses)) {
      setStatusForAvailableMutations(mutations, DetectionStatus.NOT_STARTED);
      runTestsInSeperateProcess(cp, this.testClasses, mutations);

    } else {
      setStatusForAvailableMutations(mutations, DetectionStatus.NO_COVERAGE);
    }
    reportResults(mutations, this.availableMutations, rc, loader);
  }

  private void setStatusForAvailableMutations(
      final Map<MutationDetails, MutationStatusTestPair> mutations,
      final DetectionStatus status) {
    FCollection.forEach(this.availableMutations,
        putToMap(mutations, new MutationStatusTestPair(status)));
  }

  private boolean hasTestCoverage(final Collection<ClassName> tests) {
    return !tests.isEmpty();
  }

  private void runTestInSeperateProcessForMutationRange(
      final Map<MutationDetails, MutationStatusTestPair> allmutations,
      final Collection<MutationDetails> remainingMutations,
      final Collection<ClassName> tests, final String cp) throws IOException {

    final SlaveArguments fileArgs = new SlaveArguments(remainingMutations,
        tests, this.config, this.timeoutStrategy, Log.isVerbose(),
        this.pitConfig);

    final PortFinder pf = PortFinder.INSTANCE;

    final MutationTestProcess worker = new MutationTestProcess(
        pf.getNextAvailablePort(), ProcessArgs.withClassPath(cp)
            .andJVMArgs(getJVMArgs()).andJavaAgentFinder(this.javaAgentFinder)
            .andStdout(captureStdOutIfVerbose())
            .andStderr(printWith("stderr ")), fileArgs);
    worker.start();

    setFirstMutationToStatusOfStartedInCaseSlaveFailsAtBoot(allmutations,
        remainingMutations);

    ExitCode exitCode = ExitCode.UNKNOWN_ERROR;
    try {
      exitCode = ExitCode.fromCode(worker.waitToDie());
      LOG.fine("Exit code was - " + exitCode);
    } catch (final InterruptedException e1) {
      // swallow
    }

    worker.results(allmutations);

    correctResultForProcessExitCode(allmutations, exitCode);

  }

  private SideEffect1<String> captureStdOutIfVerbose() {
    if (this.verbose) {
      return Prelude.printWith("stdout ");
    } else {
      return Prelude.noSideEffect(String.class);
    }

  }

  private void setFirstMutationToStatusOfStartedInCaseSlaveFailsAtBoot(
      final Map<MutationDetails, MutationStatusTestPair> allmutations,
      final Collection<MutationDetails> remainingMutations) {
    allmutations.put(remainingMutations.iterator().next(),
        new MutationStatusTestPair(DetectionStatus.STARTED));
  }

  private void correctResultForProcessExitCode(
      final Map<MutationDetails, MutationStatusTestPair> mutations,
      final ExitCode exitCode) {

    if (!exitCode.isOk()) {
      LOG.warning("Slave encountered error or timeout");
      final Collection<MutationDetails> unfinishedRuns = getUnfinishedRuns(mutations);
      final MutationStatusTestPair status = new MutationStatusTestPair(
          DetectionStatus.getForErrorExitCode(exitCode));
      LOG.fine("Setting " + unfinishedRuns.size() + " unfinished runs to "
          + status + " state");
      FCollection.forEach(unfinishedRuns, putToMap(mutations, status));
    } else {
      LOG.fine("Slave exited ok");
    }

  }

  private Collection<MutationDetails> getUnfinishedRuns(
      final Map<MutationDetails, MutationStatusTestPair> mutations) {

    return FCollection.flatMap(mutations.entrySet(),
        detectionStatusIs(DetectionStatus.STARTED));
  }

  private F<Entry<MutationDetails, MutationStatusTestPair>, Option<MutationDetails>> detectionStatusIs(
      final DetectionStatus status) {
    return new F<Entry<MutationDetails, MutationStatusTestPair>, Option<MutationDetails>>() {

      public Option<MutationDetails> apply(
          final Entry<MutationDetails, MutationStatusTestPair> a) {
        if (a.getValue().getStatus().equals(status)) {
          return Option.some(a.getKey());
        } else {
          return Option.none();
        }
      }

    };
  }

  private List<String> getJVMArgs() {
    return this.config.getJVMArgs();
  }

  private void runTestsInSeperateProcess(final String cp,
      final Collection<ClassName> tests,
      final Map<MutationDetails, MutationStatusTestPair> mutations)
      throws IOException, InterruptedException {

    Collection<MutationDetails> remainingMutations = getUnrunMutationIds(mutations);

    while (!remainingMutations.isEmpty()) {
      LOG.info(remainingMutations.size() + " mutations left to test");
      runTestInSeperateProcessForMutationRange(mutations, remainingMutations,
          tests, cp);
      remainingMutations = getUnrunMutationIds(mutations);
    }

  }

  private Collection<MutationDetails> getUnrunMutationIds(
      final Map<MutationDetails, MutationStatusTestPair> mutations) {

    final F<MutationDetails, Boolean> p = new F<MutationDetails, Boolean>() {

      public Boolean apply(final MutationDetails a) {
        final MutationStatusTestPair status = mutations.get(a);
        return status.getStatus().equals(DetectionStatus.NOT_STARTED);
      }

    };
    return FCollection.filter(mutations.keySet(), p);
  }

  private String createClassPath(final ClassLoader loader) {
    String cp = null;
    if (IsolationUtils.loaderAgnosticInstanceOf(loader, PITClassLoader.class)) {
      cp = getLocalClasspathFromLoader(loader);
    } else {
      cp = new ClassPath(true).getLocalClassPath();
    }

    return cp;
  }

  private String getLocalClasspathFromLoader(final ClassLoader loader) {
    final Method m = org.pitest.reflection.Reflection.publicMethod(
        loader.getClass(), "getLocalClassPath");
    try {
      return (String) m.invoke(loader);
    } catch (final Exception ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

  private void reportResults(
      final Map<MutationDetails, MutationStatusTestPair> mutations,
      final Collection<MutationDetails> availableMutations,
      final ResultCollector rc, final ClassLoader loader) {

    final FunctionalList<MutationResult> results = FCollection.map(
        availableMutations, detailsToMutationResults(mutations));

    final MetaData md = new MutationMetaData(this.config, results);

    rc.notifyEnd(this.getDescription(), md);

  }

  private F<MutationDetails, MutationResult> detailsToMutationResults(
      final Map<MutationDetails, MutationStatusTestPair> mutations) {
    return new F<MutationDetails, MutationResult>() {

      public MutationResult apply(final MutationDetails a) {
        return new MutationResult(a, mutations.get(a));
      }

    };
  }

  public MutationConfig getMutationConfig() {
    return this.config;
  }

}
