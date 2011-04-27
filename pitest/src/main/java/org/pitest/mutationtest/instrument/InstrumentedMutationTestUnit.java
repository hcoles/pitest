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

import static org.pitest.functional.Prelude.isInstanceOf;
import static org.pitest.functional.Prelude.not;
import static org.pitest.functional.Prelude.printWith;
import static org.pitest.functional.Prelude.putToMap;
import static org.pitest.util.Unchecked.translateCheckedException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.pitest.Description;
import org.pitest.MetaData;
import org.pitest.Pitest;
import org.pitest.extension.Configuration;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestFilter;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.NullDiscoveryListener;
import org.pitest.extension.common.UnGroupedStrategy;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Option;
import org.pitest.functional.SideEffect1;
import org.pitest.internal.ClassPath;
import org.pitest.internal.classloader.PITClassLoader;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus;
import org.pitest.mutationtest.instrument.ResultsReader.MutationResult;
import org.pitest.testunit.AbstractTestUnit;
import org.pitest.testunit.IgnoredTestUnit;
import org.pitest.util.ExitCode;
import org.pitest.util.Functions;
import org.pitest.util.JavaAgent;
import org.pitest.util.Log;
import org.pitest.util.WrappingProcess;

public class InstrumentedMutationTestUnit extends AbstractTestUnit {

  private final static Logger               LOG = Log.getLogger();

  private final JavaAgent                   javaAgentFinder;
  private final MutationConfig              config;
  private final TimeoutLengthStrategy       timeoutStrategy;
  private final Collection<MutationDetails> availableMutations;

  protected final Configuration             pitConfig;

  protected final Collection<String>        testClasses;

  public InstrumentedMutationTestUnit(
      final Collection<MutationDetails> availableMutations,
      final Collection<String> testClasses, final Configuration pitConfig,
      final MutationConfig mutationConfig, final Description description,
      final JavaAgent javaAgentFinder,
      final TimeoutLengthStrategy timeoutStrategy) {
    super(description);
    this.availableMutations = availableMutations;
    this.config = mutationConfig;
    this.pitConfig = pitConfig;
    this.javaAgentFinder = javaAgentFinder;
    this.timeoutStrategy = timeoutStrategy;
    this.testClasses = testClasses;

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

        final ClassPath cp = createClassPath(loader);

        final List<TestUnit> tests = findTestUnits(loader);

        if (!tests.isEmpty() && !containsOnlyIgnoredTestUnits(tests)) {

          final Map<MutationDetails, DetectionStatus> mutations = new HashMap<MutationDetails, DetectionStatus>();
          FCollection.forEach(this.availableMutations,
              putToMap(mutations, DetectionStatus.NOT_STARTED));

          runTestsInSeperateProcess(cp, tests, mutations);

          reportResults(mutations, this.availableMutations, rc, loader);

        } else {
          rc.notifyEnd(this.getDescription(), new AssertionError(
              "No tests to mutation test"));
        }

      } else {
        LOG.info("Skipping test " + this.getDescription()
            + " as no mutations found");
        rc.notifySkipped(this.getDescription());
      }
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

  protected List<TestUnit> findTestUnits(final ClassLoader loader) {
    final Collection<Class<?>> tcs = FCollection.flatMap(this.testClasses,
        Functions.stringToClass(loader));
    // FIXME we do not apply any test filters. Is this what the user
    // expects?
    return Pitest.findTestUnitsForAllSuppliedClasses(this.pitConfig,
        new NullDiscoveryListener(), new UnGroupedStrategy(),
        Option.<TestFilter> none(), tcs.toArray(new Class<?>[tcs.size()]));
  }

  private void runTestInSeperateProcessForMutationRange(
      final Map<MutationDetails, DetectionStatus> allmutations,
      final Collection<MutationDetails> remainingMutations,
      final List<TestUnit> tests, final ClassPath cp) throws IOException {

    final SlaveArguments fileArgs = new SlaveArguments(
        WrappingProcess.randomFilename(), remainingMutations, tests,
        this.config, System.getProperties(), this.timeoutStrategy);

    final MutationTestProcess worker = new MutationTestProcess(
        WrappingProcess.Args.withClassPath(cp).andJVMArgs(getJVMArgs())
            .andJavaAgentFinder(this.javaAgentFinder).andStdout(discard())
            .andStderr(printWith("SLAVE :")), fileArgs);

    setFirstMutationToStatusOfStartedInCaseSlaveFailsAtBoot(allmutations,
        remainingMutations);

    ExitCode exitCode = ExitCode.UNKNOWN_ERROR;
    try {
      exitCode = ExitCode.fromCode(worker.waitToDie());
      LOG.info("Exit code was - " + exitCode);
    } catch (final InterruptedException e1) {
      // swallow
    }

    worker.results(allmutations);

    worker.cleanUp();

    correctResultForProcessExitCode(allmutations, exitCode);

  }

  private SideEffect1<String> discard() {
    return new SideEffect1<String>() {

      public void apply(final String a) {
        System.out.println("SLAVE : " + a);
      }

    };
  }

  private void setFirstMutationToStatusOfStartedInCaseSlaveFailsAtBoot(
      final Map<MutationDetails, DetectionStatus> allmutations,
      final Collection<MutationDetails> remainingMutations) {
    allmutations.put(remainingMutations.iterator().next(),
        DetectionStatus.STARTED);
  }

  private void correctResultForProcessExitCode(
      final Map<MutationDetails, DetectionStatus> mutations,
      final ExitCode exitCode) {

    if (!exitCode.isOk()) {
      LOG.warning("Slave encountered error");
      final Collection<MutationDetails> unfinishedRuns = getUnfinishedRuns(mutations);
      final DetectionStatus status = DetectionStatus
          .getForErrorExitCode(exitCode);
      LOG.info("Setting " + unfinishedRuns.size() + " unfinished runs to "
          + status + " state");
      FCollection.forEach(unfinishedRuns, putToMap(mutations, status));
    } else {
      LOG.info("Slave exited ok");
    }

  }

  private Collection<MutationDetails> getUnfinishedRuns(
      final Map<MutationDetails, DetectionStatus> mutations) {

    return FCollection.flatMap(mutations.entrySet(),
        detectionStatusIs(DetectionStatus.STARTED));
  }

  private F<Entry<MutationDetails, DetectionStatus>, Option<MutationDetails>> detectionStatusIs(
      final DetectionStatus status) {
    return new F<Entry<MutationDetails, DetectionStatus>, Option<MutationDetails>>() {

      public Option<MutationDetails> apply(
          final Entry<MutationDetails, DetectionStatus> a) {
        if (a.getValue().equals(status)) {
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

  private void runTestsInSeperateProcess(final ClassPath cp,
      final List<TestUnit> tests,
      final Map<MutationDetails, DetectionStatus> mutations)
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
      final Map<MutationDetails, DetectionStatus> mutations) {

    final F<MutationDetails, Boolean> p = new F<MutationDetails, Boolean>() {

      public Boolean apply(final MutationDetails a) {
        final DetectionStatus status = mutations.get(a);
        return status.equals(DetectionStatus.NOT_STARTED);
      }

    };
    return FCollection.filter(mutations.keySet(), p);
  }

  private ClassPath createClassPath(final ClassLoader loader) {
    ClassPath cp = null;
    if (loader instanceof PITClassLoader) {
      cp = ((PITClassLoader) loader).getClassPath();
    } else {
      cp = new ClassPath();
    }
    return cp;
  }

  private void reportResults(
      final Map<MutationDetails, DetectionStatus> mutations,
      final Collection<MutationDetails> availableMutations,
      final ResultCollector rc, final ClassLoader loader) {

    final FunctionalList<MutationResult> results = FCollection.map(
        availableMutations, detailsToMutationResults(mutations));

    final MetaData md = new MutationMetaData(this.config,
        uniqueMutatedClasses(), results);

    final List<AssertionError> failures = results.filter(mutationNotDetected())
        .map(resultToAssertionError());

    final float percentageDetected = 100f - ((failures.size() / (float) mutations
        .size()) * 100f);
    if (percentageDetected < this.config.getThreshold()) {

      final AssertionError ae = new AssertionError("Tests detected "
          + percentageDetected + "% of " + mutations.size()
          + " mutations. Threshold was " + this.config.getThreshold());

      AssertionError last = ae;
      for (final AssertionError each : failures) {
        last.initCause(each);
        last = each;
      }
      rc.notifyEnd(this.getDescription(), ae, md);
    } else {
      rc.notifyEnd(this.getDescription(), md);
    }

  }

  private Collection<String> uniqueMutatedClasses() {
    final Set<String> classes = new HashSet<String>();
    FCollection.mapTo(this.availableMutations, mutationToMutee(), classes);
    return classes;
  }

  private F<MutationDetails, String> mutationToMutee() {
    return new F<MutationDetails, String>() {
      public String apply(final MutationDetails a) {
        return a.getClazz();
      }
    };
  }

  private F<MutationDetails, MutationResult> detailsToMutationResults(
      final Map<MutationDetails, DetectionStatus> mutations) {
    return new F<MutationDetails, MutationResult>() {

      public MutationResult apply(final MutationDetails a) {
        return new MutationResult(a, mutations.get(a));
      }

    };
  }

  private F<MutationResult, AssertionError> resultToAssertionError() {
    return new F<MutationResult, AssertionError>() {

      public AssertionError apply(final MutationResult result) {
        final AssertionError ae = new AssertionError("The mutation -> "
            + result.details + " did not result in any test failures");
        final StackTraceElement[] stackTrace = { result.details
            .stackTraceDescription() };
        ae.setStackTrace(stackTrace);
        return ae;
      }

    };
  }

  private F<MutationResult, Boolean> mutationNotDetected() {
    return new F<MutationResult, Boolean>() {

      public Boolean apply(final MutationResult a) {
        return !a.status.isDetected();
      }

    };
  }

  private boolean containsOnlyIgnoredTestUnits(final List<TestUnit> tests) {
    // FIXME handle this more generically
    return !FCollection.contains(tests,
        not(isInstanceOf(IgnoredTestUnit.class)));
  }

  public MutationConfig getMutationConfig() {
    return this.config;
  }

}
