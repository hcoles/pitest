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
package org.pitest;

import org.apache.maven.plugin.MojoExecutionException;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.DefaultCoverageDatabase;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.execute.LaunchOptions;
import org.pitest.functional.FCollection;
import org.pitest.internal.ClassPath;
import org.pitest.internal.ClassPathByteArraySource;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.classloader.DefaultPITClassloader;
import org.pitest.mutationtest.CompoundListenerFactory;
import org.pitest.mutationtest.MutationClassPaths;
import org.pitest.mutationtest.MutationCoverageReport;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.Timings;
import org.pitest.mutationtest.instrument.JarCreatingJarFinder;
import org.pitest.mutationtest.instrument.KnownLocationJavaAgentFinder;
import org.pitest.mutationtest.report.DatedDirectoryResultOutputStrategy;
import org.pitest.mutationtest.report.OutputFormat;
import org.pitest.mutationtest.report.ResultOutputStrategy;
import org.pitest.mutationtest.verify.DefaultBuildVerifier;
import org.pitest.util.JavaAgent;

public class RunPitStrategy implements GoalStrategy {

  public void execute(final ReportOptions data) throws MojoExecutionException {

    System.out.println("Running report with " + data);
    final ClassPath cp = data.getClassPath();

    // workaround for apparent java 1.5 JVM bug . . . might not play nicely
    // with distributed testing
    final JavaAgent jac = new JarCreatingJarFinder(new ClassPathByteArraySource(cp));
    final KnownLocationJavaAgentFinder ja = new KnownLocationJavaAgentFinder(
        jac.getJarLocation().value());

    final ResultOutputStrategy reportOutput = new DatedDirectoryResultOutputStrategy(
        data.getReportDir());
    final CompoundListenerFactory reportFactory = new CompoundListenerFactory(
        FCollection.map(data.getOutputFormats(),
            OutputFormat.createFactoryForFormat(reportOutput)));

    CoverageOptions coverageOptions = data.createCoverageOptions();
    LaunchOptions launchOptions = new LaunchOptions(ja, data.getJvmArgs());
    MutationClassPaths cps = data.getMutationClassPaths();

    Timings timings = new Timings();
    final CoverageDatabase coverageDatabase = new DefaultCoverageDatabase(
        coverageOptions, launchOptions, cps, timings);
    final MutationCoverageReport report = new MutationCoverageReport(
        coverageDatabase, data, reportFactory, timings, new DefaultBuildVerifier());

    // Create new classloader under boot
    final ClassLoader loader = new DefaultPITClassloader(cp,
        IsolationUtils.bootClassLoader());
    final ClassLoader original = IsolationUtils.getContextClassLoader();

    try {
      IsolationUtils.setContextClassLoader(loader);

      final Runnable run = (Runnable) IsolationUtils.cloneForLoader(report,
          loader);

      run.run();

    } catch (final Exception e) {
      throw new MojoExecutionException("fail", e);
    } finally {
      IsolationUtils.setContextClassLoader(original);
      jac.close();
      ja.close();

    }
  }

}
