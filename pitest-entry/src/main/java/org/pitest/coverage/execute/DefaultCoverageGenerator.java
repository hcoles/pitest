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

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.pitest.classinfo.ClassInfo;
import org.pitest.classpath.CodeSource;
import org.pitest.coverage.CoverageData;
import org.pitest.coverage.CoverageExporter;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.coverage.CoverageResult;
import org.pitest.coverage.analysis.LineMapper;
import org.pitest.functional.FCollection;
import org.pitest.functional.SideEffect1;
import org.pitest.functional.prelude.Prelude;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;
import org.pitest.mutationtest.config.TestPluginArguments;
import org.pitest.process.LaunchOptions;
import org.pitest.process.ProcessArgs;
import org.pitest.util.ExitCode;
import org.pitest.util.Log;
import org.pitest.util.PitError;
import org.pitest.util.SocketFinder;
import org.pitest.util.StringUtil;
import org.pitest.util.Timings;
import org.pitest.util.Unchecked;

public class DefaultCoverageGenerator implements CoverageGenerator {

  private static final Logger    LOG = Log.getLogger();

  private final CoverageOptions  coverageOptions;
  private final LaunchOptions    launchOptions;
  private final CodeSource       code;
  private final Timings          timings;
  private final File             workingDir;
  private final CoverageExporter exporter;
  private final boolean          showProgress;

  public DefaultCoverageGenerator(final File workingDir,
      final CoverageOptions coverageOptions, final LaunchOptions launchOptions,
      final CodeSource code, final CoverageExporter exporter,
      final Timings timings, final boolean showProgress) {
    this.coverageOptions = coverageOptions;
    this.code = code;
    this.launchOptions = launchOptions;
    this.timings = timings;
    this.workingDir = workingDir;
    this.exporter = exporter;
    this.showProgress = showProgress;
  }

  @Override
  public CoverageData calculateCoverage() {
    try {
      final long t0 = System.currentTimeMillis();

      this.timings.registerStart(Timings.Stage.SCAN_CLASS_PATH);
      final Collection<ClassInfo> tests = this.code.getTests();
      this.timings.registerEnd(Timings.Stage.SCAN_CLASS_PATH);

      final CoverageData coverage = new CoverageData(this.code, new LineMapper(
          this.code));

      this.timings.registerStart(Timings.Stage.COVERAGE);
      gatherCoverageData(tests, coverage);
      this.timings.registerEnd(Timings.Stage.COVERAGE);

      final long time = (System.currentTimeMillis() - t0) / 1000;

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
          + coverage.getFailingTestDescriptions().stream().map(test -> test.toString())
          .collect(Collectors.joining(StringUtil.newLine())));
      throw new PitHelpError(Help.FAILING_TESTS, coverage.getCountFailedTests());
    }
  }

  private void gatherCoverageData(final Collection<ClassInfo> tests,
      final CoverageData coverage) throws IOException, InterruptedException,
      ExecutionException {

    final List<String> filteredTests = FCollection
        .map(tests, classInfoToName());

    final SideEffect1<CoverageResult> handler = resultProcessor(coverage);

    final SocketFinder sf = new SocketFinder();
    final ServerSocket socket = sf.getNextAvailableServerSocket();

    final CoverageProcess process = new CoverageProcess(ProcessArgs
        .withClassPath(this.code.getClassPath()).andBaseDir(this.workingDir)
        .andLaunchOptions(this.launchOptions).andStderr(logInfo())
        .andStdout(captureStandardOutIfVerbose()), this.coverageOptions,
        socket, filteredTests, handler);

    process.start();

    final ExitCode exitCode = process.waitToDie();

    if (exitCode == ExitCode.JUNIT_ISSUE) {
      LOG.severe("Error generating coverage. Please check that your classpath contains JUnit 4.6 or above.");
      throw new PitError(
          "Coverage generation minion exited abnormally. Please check the classpath.");
    } else if (!exitCode.isOk()) {
      LOG.severe("Coverage generator Minion exited abnormally due to "
          + exitCode);
      throw new PitError("Coverage generation minion exited abnormally!");
    } else {
      LOG.fine("Coverage generator Minion exited ok");
    }
  }

  private static Function<ClassInfo, String> classInfoToName() {
    return a -> a.getName().asInternalName();
  }

  private SideEffect1<String> captureStandardOutIfVerbose() {
    if (this.coverageOptions.isVerbose()) {
      return log();
    } else {
      return Prelude.noSideEffect(String.class);
    }
  }

  private static SideEffect1<String> logInfo() {
    return a -> LOG.info("MINION : " + a);
  }

  private static SideEffect1<String> log() {
    return a -> LOG.fine("MINION : " + a);
  }

  private SideEffect1<CoverageResult> resultProcessor(
      final CoverageData coverage) {
    return new SideEffect1<CoverageResult>() {
      private final String[] spinner = new String[] { "\u0008/", "\u0008-",
          "\u0008\\", "\u0008|" };
      int i = 0;

      @Override
      public void apply(final CoverageResult cr) {
        coverage.calculateClassCoverage(cr);
        if (DefaultCoverageGenerator.this.showProgress) {
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
