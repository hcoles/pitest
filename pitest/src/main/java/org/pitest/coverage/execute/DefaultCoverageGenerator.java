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

import org.pitest.classinfo.ClassInfo;
import org.pitest.classpath.CodeSource;
import org.pitest.coverage.CoverageData;
import org.pitest.coverage.CoverageExporter;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.coverage.CoverageResult;
import org.pitest.coverage.analysis.LineMapper;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.SideEffect1;
import org.pitest.functional.prelude.Prelude;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;
import org.pitest.process.LaunchOptions;
import org.pitest.process.ProcessArgs;
import org.pitest.testapi.Configuration;
import org.pitest.util.ExitCode;
import org.pitest.util.Log;
import org.pitest.util.PitError;
import org.pitest.util.SocketFinder;
import org.pitest.util.Timings;
import org.pitest.util.Unchecked;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class DefaultCoverageGenerator implements CoverageGenerator {

  private static final Logger LOG = Log.getLogger();

  private final CoverageOptions  coverageOptions;
  private final LaunchOptions    launchOptions;
  private final CodeSource       code;
  private final Timings          timings;
  private final File             workingDir;
  private final CoverageExporter exporter;
  private final boolean          showProgress;

  public DefaultCoverageGenerator(File workingDir,
                                  CoverageOptions coverageOptions,
                                  LaunchOptions launchOptions,
                                  CodeSource code,
                                  CoverageExporter exporter,
                                  Timings timings,
                                  boolean showProgress) {
    this.coverageOptions = coverageOptions;
    this.code = code;
    this.launchOptions = launchOptions;
    this.timings = timings;
    this.workingDir = workingDir;
    this.exporter = exporter;
    this.showProgress = showProgress;
  }

  public CoverageData calculateCoverage() {
    try {
      long startTime = System.currentTimeMillis();

      timings.registerStart(Timings.Stage.SCAN_CLASS_PATH);
      Collection<ClassInfo> tests = code.getTests();
      timings.registerEnd(Timings.Stage.SCAN_CLASS_PATH);

      CoverageData coverage = new CoverageData(code, new LineMapper(code));

      timings.registerStart(Timings.Stage.COVERAGE);
      gatherCoverageData(tests, coverage);
      timings.registerEnd(Timings.Stage.COVERAGE);

      long time = (System.currentTimeMillis() - startTime) / 1000;

      LOG.info("Calculated coverage in " + time + " seconds.");

      verifyBuildSuitableForMutationTesting(coverage);

      exporter.recordCoverage(coverage.createCoverage());

      return coverage;
    } catch (PitHelpError phe) {
      throw phe;
    } catch (Exception e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  private void verifyBuildSuitableForMutationTesting(CoverageData coverage) {
    if (!coverage.allTestsGreen()) {
      throw new PitHelpError(Help.FAILING_TESTS);
    }
  }

  private void gatherCoverageData(Collection<ClassInfo> tests,
                                  CoverageData coverage)
      throws IOException, InterruptedException, ExecutionException {

    List<String> filteredTests = FCollection.map(tests, classInfoToName());
    SideEffect1<CoverageResult> handler = resultProcessor(coverage);

    SocketFinder sf = new SocketFinder();
    ServerSocket socket = sf.getNextAvailableServerSocket();

    ProcessArgs processArgs = ProcessArgs.withClassPath(code.getClassPath())
                                          .andBaseDir(workingDir)
                                          .andLaunchOptions(launchOptions)
                                          .andStderr(logInfo())
                                          .andStdout(captureStandardOutIfVerbose());

    CoverageProcess process = new CoverageProcess( processArgs,
                                                   coverageOptions,
                                                   socket,
                                                   filteredTests,
                                                   handler);

    process.start();

    ExitCode exitCode = process.waitToDie();

    if (exitCode == ExitCode.JUNIT_ISSUE) {
      LOG.severe("Error generating coverage. Please check that your classpath contains JUnit 4.6 or above.");
      throw new PitError("Coverage generation slave exited abnormally. Please check the classpath.");
    } else if (!exitCode.isOk()) {
      LOG.severe("Coverage generator Slave exited abnormally due to " + exitCode);
      throw new PitError("Coverage generation slave exited abnormally!");
    } else {
      LOG.fine("Coverage generator Slave exited ok");
    }
  }

  private static F<ClassInfo, String> classInfoToName() {
    return new F<ClassInfo, String>() {
      public String apply(ClassInfo a) {
        return a.getName().asInternalName();
      }
    };
  }

  private SideEffect1<String> captureStandardOutIfVerbose() {
    if (coverageOptions.isVerbose()) {
      return log();
    } else {
      return Prelude.noSideEffect(String.class);
    }
  }

  private SideEffect1<String> logInfo() {
    return new SideEffect1<String>() {
      public void apply(String a) {
        LOG.info("SLAVE : " + a);
      }
    };
  }

  private SideEffect1<String> log() {
    return new SideEffect1<String>() {
      public void apply(String a) {
        LOG.fine("SLAVE : " + a);
      }
    };
  }

  private SideEffect1<CoverageResult> resultProcessor(final CoverageData coverage) {
    return new SideEffect1<CoverageResult>() {
      private String[] spinner = new String[] { "\u0008/", "\u0008-", "\u0008\\", "\u0008|" };
      int i = 0;

      public void apply(CoverageResult cr) {
        coverage.calculateClassCoverage(cr);
        if (showProgress) {
          System.out.printf("%s", spinner[i % spinner.length]);
        }
        i++;
      }
    };
  }

  public Configuration getConfiguration() {
    return coverageOptions.getPitConfig();
  }

  public LaunchOptions getLaunchOptions() {
    return launchOptions;
  }
}
