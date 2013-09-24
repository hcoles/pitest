package org.pitest.mutationtest.tooling;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassPathByteArraySource;
import org.pitest.classpath.CodeSource;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.execute.DefaultCoverageGenerator;
import org.pitest.coverage.execute.LaunchOptions;
import org.pitest.functional.Option;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.mutationtest.MutationCoverage;
import org.pitest.mutationtest.MutationStrategies;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.SettingsFactory;
import org.pitest.mutationtest.incremental.HistoryStore;
import org.pitest.mutationtest.incremental.WriterFactory;
import org.pitest.mutationtest.incremental.XStreamHistoryStore;
import org.pitest.mutationtest.instrument.JarCreatingJarFinder;
import org.pitest.mutationtest.instrument.KnownLocationJavaAgentFinder;
import org.pitest.process.JavaAgent;
import org.pitest.util.ResultOutputStrategy;
import org.pitest.util.Timings;

public class EntryPoint {

  /**
   * Convenient entry point for tools to run mutation analysis.
   * 
   * The big grab bag of config stored in ReportOptions must be setup correctly
   * first.
   * 
   */
  public AnalysisResult execute(final File baseDir, final ReportOptions data) {

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

    final MutationResultListenerFactory reportFactory = settings.createListener();
        
    final CoverageOptions coverageOptions = data.createCoverageOptions();
    final LaunchOptions launchOptions = new LaunchOptions(ja, data.getJvmArgs());
    final ProjectClassPaths cps = data.getMutationClassPaths();

    final CodeSource code = new CodeSource(cps, coverageOptions.getPitConfig()
        .testClassIdentifier());

    final Timings timings = new Timings();
    final CoverageGenerator coverageDatabase = new DefaultCoverageGenerator(
        baseDir, coverageOptions, launchOptions, code,
        settings.createCoverageExporter(), timings, !data.isVerbose());

    final HistoryStore history = new XStreamHistoryStore(historyWriter, reader);

    final MutationStrategies strategies = new MutationStrategies(
        settings.createEngine(), history, coverageDatabase, reportFactory, reportOutput);

    final MutationCoverage report = new MutationCoverage(strategies, baseDir,
        code, data, timings);

    try {
      return AnalysisResult.success(report.runReport());
    } catch (final IOException e) {
      return AnalysisResult.fail(e);
    } finally {
      jac.close();
      ja.close();
      historyWriter.close();
    }
  }
  
}
