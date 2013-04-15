/*
 * Copyright 2011 Henry Coles
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
package org.pitest.maven;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.apache.maven.plugin.MojoExecutionException;
import org.pitest.classinfo.CodeSource;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.coverage.DefaultCoverageGenerator;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.execute.LaunchOptions;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.internal.ClassPath;
import org.pitest.internal.ClassPathByteArraySource;
import org.pitest.mutationtest.CompoundListenerFactory;
import org.pitest.mutationtest.MutationClassPaths;
import org.pitest.mutationtest.MutationCoverage;
import org.pitest.mutationtest.MutationStrategies;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.SettingsFactory;
import org.pitest.mutationtest.Timings;
import org.pitest.mutationtest.incremental.HistoryStore;
import org.pitest.mutationtest.incremental.WriterFactory;
import org.pitest.mutationtest.incremental.XStreamHistoryStore;
import org.pitest.mutationtest.instrument.JarCreatingJarFinder;
import org.pitest.mutationtest.instrument.KnownLocationJavaAgentFinder;
import org.pitest.mutationtest.report.OutputFormat;
import org.pitest.mutationtest.report.ResultOutputStrategy;
import org.pitest.mutationtest.statistics.MutationStatistics;
import org.pitest.util.JavaAgent;

public class RunPitStrategy implements GoalStrategy {

  public MutationStatistics execute(final File baseDir, final ReportOptions data)
      throws MojoExecutionException {

    final SettingsFactory settings = new SettingsFactory(data);

    final ClassPath cp = data.getClassPath();

    final Option<Reader> reader = data.createHistoryReader();
    final WriterFactory historyWriter = data.createHistoryWriter();

    // workaround for apparent java 1.5 JVM bug . . . might not play nicely
    // with distributed testing
    final JavaAgent jac = new JarCreatingJarFinder(
        new ClassPathByteArraySource(cp));
    final KnownLocationJavaAgentFinder ja = new KnownLocationJavaAgentFinder(
        jac.getJarLocation().value());

    final ResultOutputStrategy reportOutput = settings.getOutputStrategy();

    final CompoundListenerFactory reportFactory = new CompoundListenerFactory(
        FCollection.map(data.getOutputFormats(),
            OutputFormat.createFactoryForFormat(reportOutput)));

    final CoverageOptions coverageOptions = data.createCoverageOptions();
    final LaunchOptions launchOptions = new LaunchOptions(ja, data.getJvmArgs());
    final MutationClassPaths cps = data.getMutationClassPaths();

    final CodeSource code = new CodeSource(cps, coverageOptions.getPitConfig()
        .testClassIdentifier());

    final Timings timings = new Timings();
    final CoverageGenerator coverageDatabase = new DefaultCoverageGenerator(
        baseDir, coverageOptions, launchOptions, code,
        settings.createCoverageExporter(), timings, !data.isVerbose());

    final HistoryStore history = new XStreamHistoryStore(historyWriter, reader);

    MutationStrategies strategies = new MutationStrategies(settings.createEngine(),history, coverageDatabase, reportFactory);
    
    final MutationCoverage report = new MutationCoverage(strategies, baseDir, 
        code, data,  timings);

    try {    
      return report.runReport();
    } catch (final IOException e) {
      throw new MojoExecutionException("fail", e);
    } finally {
      jac.close();
      ja.close();
      historyWriter.close();
    }
  }

}
