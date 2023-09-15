/*
 * Copyright 2012 Henry Coles
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

package org.pitest.coverage.execute;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.CodeSource;
import org.pitest.coverage.CoverageData;
import org.pitest.coverage.CoverageExporter;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.coverage.CoverageResult;
import org.pitest.coverage.analysis.LineMapper;
import org.pitest.functional.prelude.Prelude;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;
import org.pitest.mutationtest.config.TestPluginArguments;
import org.pitest.process.LaunchOptions;
import org.pitest.process.ProcessArgs;
import org.pitest.testapi.Description;
import org.pitest.util.ExitCode;
import org.pitest.util.Log;
import org.pitest.util.PitError;
import org.pitest.util.SocketFinder;
import org.pitest.util.StringUtil;
import org.pitest.util.Timings;
import org.pitest.util.Unchecked;
import org.pitest.util.Verbosity;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class DefaultCoverageGenerator implements CoverageGenerator {

  private static final Logger    LOG = Log.getLogger();

  private final CoverageOptions  coverageOptions;
  private final LaunchOptions    launchOptions;
  private final CodeSource       code;
  private final Timings          timings;
  private final File             workingDir;
  private final CoverageExporter exporter;
  private final Verbosity        verbosity;

  public DefaultCoverageGenerator(final File workingDir,
      final CoverageOptions coverageOptions, final LaunchOptions launchOptions,
      final CodeSource code, final CoverageExporter exporter,
      final Timings timings, Verbosity verbosity) {
    this.coverageOptions = coverageOptions;
    this.code = code;
    this.launchOptions = launchOptions;
    this.timings = timings;
    this.workingDir = workingDir;
    this.exporter = exporter;
    this.verbosity = verbosity;
  }

  @Override
  public CoverageData calculateCoverage(Predicate<ClassName> testFilter) {
    try {
      final long t0 = System.nanoTime();

      this.timings.registerStart(Timings.Stage.SCAN_CLASS_PATH);
      List<String> tests = this.code.testTrees()
              .map(ClassTree::name)
              .filter(testFilter)
              .map(ClassName::asInternalName)
              .collect(Collectors.toList());

      this.timings.registerEnd(Timings.Stage.SCAN_CLASS_PATH);

      final CoverageData coverage = new CoverageData(this.code, new LineMapper(
          this.code));

      this.timings.registerStart(Timings.Stage.COVERAGE);
      if (tests.isEmpty()) {
        // This may happen as a result of filtering for incremental analysis as well as
        // simple misconfiguration.
        LOG.info("No test classes identified to scan");
      } else {
        gatherCoverageData(tests, coverage);
      }
      this.timings.registerEnd(Timings.Stage.COVERAGE);

      final long time = NANOSECONDS.toSeconds(System.nanoTime() - t0);

      LOG.info("Calculated coverage in " + time + " seconds.");

      verifyBuildSuitableForMutationTesting(coverage);

      this.exporter.recordCoverage(coverage.createCoverage());

      return coverage;

    } catch (final PitHelpError phe) {
      throw phe;
    } catch (final Exception e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  private static void verifyBuildSuitableForMutationTesting(final CoverageData coverage) {
    if (!coverage.allTestsGreen()) {
      LOG.severe("Tests failing without mutation: " + StringUtil.newLine()
          + coverage.getFailingTestDescriptions().stream().map(Description::toString)
          .collect(Collectors.joining(StringUtil.newLine())));
      throw new PitHelpError(Help.FAILING_TESTS, coverage.getCountFailedTests());
    }
  }

  private void gatherCoverageData(List<String> tests,
      final CoverageData coverage) throws IOException, InterruptedException {

    final Consumer<CoverageResult> handler = resultProcessor(coverage);

    final SocketFinder sf = new SocketFinder();
    final ServerSocket socket = sf.getNextAvailableServerSocket();

    final CoverageProcess process = new CoverageProcess(ProcessArgs
        .withClassPath(this.code.getClassPath()).andBaseDir(this.workingDir)
        .andLaunchOptions(this.launchOptions).andStderr(logInfo())
        .andStdout(captureStandardOutIfVerbose()), this.coverageOptions,
        socket, tests, handler);

    process.start();

    final ExitCode exitCode = process.waitToDie();

    if (exitCode == ExitCode.TEST_PLUGIN_ISSUE) {
      LOG.severe("Pitest could not run any tests. Please check that you have installed the pitest plugin for your testing library (eg JUnit 5, TestNG). If your project uses JUnit 4 "
              + "the plugin is automatically included, but a recent version of JUnit 4 must be on the classpath.");
      throw new PitError(
          "Please check you have correctly installed the pitest plugin for your project's test library (JUnit 5, TestNG, JUnit 4 etc). ");
    } else if (!exitCode.isOk()) {
      LOG.severe("Coverage generator Minion exited abnormally due to "
          + exitCode);
      throw new PitError("Coverage generation minion exited abnormally! (" + exitCode + ")");
    } else {
      LOG.fine("Coverage generator Minion exited ok");
    }
  }

  private Consumer<String> captureStandardOutIfVerbose() {
    if (this.verbosity.showMinionOutput()) {
      return log();
    } else {
      return Prelude.noSideEffect(String.class);
    }
  }

  private static Consumer<String> logInfo() {
    return a -> LOG.info("MINION : " + a);
  }

  private static Consumer<String> log() {
    return a -> LOG.fine("MINION : " + a);
  }

  private Consumer<CoverageResult> resultProcessor(
      final CoverageData coverage) {
    return new Consumer<CoverageResult>() {
      private final String[] spinner = new String[] { "\u0008/", "\u0008-",
          "\u0008\\", "\u0008|" };
      int i = 0;

      @Override
      public void accept(final CoverageResult cr) {
        if (cr.isGreenTest() || !coverageOptions.getPitConfig().skipFailingTests()) {
          coverage.calculateClassCoverage(cr);
        }
        if (DefaultCoverageGenerator.this.verbosity.showSpinner()) {
          System.out.printf("%s", this.spinner[this.i % this.spinner.length]);
        }
        this.i++;
      }

    };
  }

  @Override
  public TestPluginArguments getConfiguration() {
    return this.coverageOptions.getPitConfig();
  }

  @Override
  public LaunchOptions getLaunchOptions() {
    return this.launchOptions;
  }

}
