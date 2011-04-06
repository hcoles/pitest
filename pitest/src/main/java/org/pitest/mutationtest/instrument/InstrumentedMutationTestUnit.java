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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.pitest.Description;
import org.pitest.MetaData;
import org.pitest.PitError;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassInfoVisitor;
import org.pitest.extension.Configuration;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Option;
import org.pitest.functional.SideEffect1;
import org.pitest.internal.ClassPath;
import org.pitest.internal.ClassPathByteArraySource;
import org.pitest.internal.classloader.PITClassLoader;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus;
import org.pitest.mutationtest.instrument.ResultsReader.MutationResult;
import org.pitest.testunit.AbstractTestUnit;
import org.pitest.testunit.IgnoredTestUnit;
import org.pitest.util.ExitCode;
import org.pitest.util.JavaAgent;
import org.pitest.util.Log;
import org.pitest.util.WrappingProcess;

public class InstrumentedMutationTestUnit extends AbstractTestUnit {

  private final static Logger         LOG = Log.getLogger();

  private final JavaAgent             javaAgentFinder;
  private final Collection<String>    classesToMutate;
  private final MutationConfig        config;
  private final CoverageSource        coverageSource;
  private final TimeoutLengthStrategy timeoutStrategy;

  public InstrumentedMutationTestUnit(final Collection<String> tests,
      final Collection<String> classesToMutate,
      final JavaAgent javaAgentFinder, final MutationConfig mutationConfig,
      final Configuration pitConfig, final Description description) {
    this(classesToMutate, mutationConfig, description, javaAgentFinder,
        new NoCoverageSource(tests, pitConfig),
        new PercentAndConstantTimeoutStrategy(1.25f, 1000));
  }

  public InstrumentedMutationTestUnit(final Collection<String> classesToMutate,
      final MutationConfig mutationConfig, final Description description,
      final JavaAgent javaAgentFinder, final CoverageSource coverageSource,
      final TimeoutLengthStrategy timeoutStrategy) {
    super(description);
    this.classesToMutate = classesToMutate;
    // this.testClasses.addAll(tests);
    this.config = mutationConfig;
    this.javaAgentFinder = javaAgentFinder;
    this.coverageSource = coverageSource;
    this.timeoutStrategy = timeoutStrategy;

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

    final Mutater m = this.config.createMutator(loader);
    final Collection<MutationDetails> availableMutations = m
        .findMutations(this.classesToMutate);

    try {
      if (!availableMutations.isEmpty()) {
        // should test unit perhaps have PitClassloader in it's interface?
        final ClassPath cp = createClassPath(loader);

        final List<TestUnit> tests = this.coverageSource.getTests(loader);

        if (!tests.isEmpty() && !containsOnlyIgnoredTestUnits(tests)) {

          final Map<MutationIdentifier, DetectionStatus> mutations = new HashMap<MutationIdentifier, DetectionStatus>();
          FCollection.map(availableMutations, mutationDetailsToId()).forEach(
              putToMap(mutations, DetectionStatus.NOT_STARTED));

          final Option<Statistics> stats = runTestsInSeperateProcess(cp, tests,
              mutations);

          reportResults(mutations, availableMutations, rc, stats, loader);

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

  private F<MutationDetails, MutationIdentifier> mutationDetailsToId() {
    return new F<MutationDetails, MutationIdentifier>() {

      public MutationIdentifier apply(final MutationDetails a) {
        return a.getId();
      }

    };
  }

  private Option<Statistics> runTestInSeperateProcessForMutationRange(
      final Map<MutationIdentifier, DetectionStatus> allmutations,
      final Collection<MutationIdentifier> remainingMutations,
      final List<TestUnit> tests, final ClassPath cp,
      final Option<Statistics> stats) throws IOException {

    final SlaveArguments fileArgs = new SlaveArguments(
        WrappingProcess.randomFilename(), remainingMutations, tests, stats,
        this.config, System.getProperties(), this.timeoutStrategy,
        this.classesToMutate);

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

    final Option<Statistics> newStats = worker.results(allmutations, stats);

    worker.cleanUp();

    correctResultForProcessExitCode(allmutations, exitCode);
    return newStats;
  }

  private SideEffect1<String> discard() {
    return new SideEffect1<String>() {

      public void apply(final String a) {
        System.out.println("SLAVE : " + a);
      }

    };
  }

  private void setFirstMutationToStatusOfStartedInCaseSlaveFailsAtBoot(
      final Map<MutationIdentifier, DetectionStatus> allmutations,
      final Collection<MutationIdentifier> remainingMutations) {
    allmutations.put(remainingMutations.iterator().next(),
        DetectionStatus.STARTED);
  }

  private void correctResultForProcessExitCode(
      final Map<MutationIdentifier, DetectionStatus> mutations,
      final ExitCode exitCode) {

    if (!exitCode.isOk()) {
      LOG.warning("Slave encountered error");
      final Collection<MutationIdentifier> unfinishedRuns = getUnfinishedRuns(mutations);
      final DetectionStatus status = DetectionStatus
          .getForErrorExitCode(exitCode);
      LOG.info("Setting " + unfinishedRuns.size() + " unfinished runs to "
          + status + " state");
      FCollection.forEach(unfinishedRuns, putToMap(mutations, status));
    } else {
      LOG.info("Slave exited ok");
    }

  }

  private Collection<MutationIdentifier> getUnfinishedRuns(
      final Map<MutationIdentifier, DetectionStatus> mutations) {

    return FCollection.flatMap(mutations.entrySet(),
        detectionStatusIs(DetectionStatus.STARTED));
  }

  private F<Entry<MutationIdentifier, DetectionStatus>, Option<MutationIdentifier>> detectionStatusIs(
      final DetectionStatus status) {
    return new F<Entry<MutationIdentifier, DetectionStatus>, Option<MutationIdentifier>>() {

      public Option<MutationIdentifier> apply(
          final Entry<MutationIdentifier, DetectionStatus> a) {
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

  private Option<Statistics> runTestsInSeperateProcess(final ClassPath cp,
      final List<TestUnit> tests,
      final Map<MutationIdentifier, DetectionStatus> mutations)
      throws IOException, InterruptedException {

    Option<Statistics> stats = this.coverageSource.getStatistics(tests,
        this.classesToMutate);

    Collection<MutationIdentifier> remainingMutations = getUnrunMutationIds(mutations);

    while (!remainingMutations.isEmpty()) {
      LOG.info(remainingMutations.size() + " mutations left to test");
      stats = runTestInSeperateProcessForMutationRange(mutations,
          remainingMutations, tests, cp, stats);
      if (stats.hasSome() && !stats.value().isGreenSuite()) {
        LOG.severe("Tests do not run green when no mutation present");
        throw new PitError(
            "Cannot mutation test as tests do not pass without mutation");
      }

      remainingMutations = getUnrunMutationIds(mutations);
    }

    return stats;

  }

  private Collection<MutationIdentifier> getUnrunMutationIds(
      final Map<MutationIdentifier, DetectionStatus> mutations) {

    final F<MutationIdentifier, Boolean> p = new F<MutationIdentifier, Boolean>() {

      public Boolean apply(final MutationIdentifier a) {
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
      final Map<MutationIdentifier, DetectionStatus> mutations,
      final Collection<MutationDetails> availableMutations,
      final ResultCollector rc, final Option<Statistics> sr,
      final ClassLoader loader) {

    final FunctionalList<MutationResult> results = FCollection.map(
        availableMutations, detailsToMutationResults(mutations));

    final MetaData md = new MutationMetaData(this.config, namesToClassInfo(
        this.classesToMutate, loader), sr, results);

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

  private F<MutationDetails, MutationResult> detailsToMutationResults(
      final Map<MutationIdentifier, DetectionStatus> mutations) {
    return new F<MutationDetails, MutationResult>() {

      public MutationResult apply(final MutationDetails a) {
        return new MutationResult(a, mutations.get(a.getId()));
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

  private Collection<ClassInfo> namesToClassInfo(
      final Collection<String> classes, final ClassLoader loader) {
    return FCollection.flatMap(classes, nameToClassInfo(loader));
  }

  private F<String, Option<ClassInfo>> nameToClassInfo(final ClassLoader loader) {
    return new F<String, Option<ClassInfo>>() {

      public Option<ClassInfo> apply(final String a) {
        final ClassPathByteArraySource source = new ClassPathByteArraySource(
            createClassPath(loader));
        final Option<byte[]> bytes = source.apply(a);
        if (bytes.hasSome()) {
          return Option.some(ClassInfoVisitor.getClassInfo(a, bytes.value()));
        } else {
          return Option.none();
        }
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
