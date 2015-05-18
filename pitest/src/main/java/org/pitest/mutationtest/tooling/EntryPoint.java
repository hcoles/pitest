package org.pitest.mutationtest.tooling;

import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassPathByteArraySource;
import org.pitest.classpath.CodeSource;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.execute.DefaultCoverageGenerator;
import org.pitest.mutationtest.HistoryStore;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.mutationtest.incremental.XStreamHistoryStore;
import org.pitest.process.JavaAgent;
import org.pitest.process.LaunchOptions;
import org.pitest.util.ResultOutputStrategy;
import org.pitest.util.Timings;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class EntryPoint {

  /**
   * Convenient entry point for tools to run mutation analysis.
   *
   * The big grab bag of config stored in ReportOptions must be setup correctly
   * first.
   *  @param baseDir
   *          directory from which analysis will be run
   * @param data
   * @param environmentVariables
   *
   */
  public AnalysisResult execute(File baseDir,
                                ReportOptions data,
                                PluginServices plugins,
                                Map<String, String> environmentVariables) {
    SettingsFactory settings = new SettingsFactory(data, plugins);
    return execute(baseDir, data, settings,environmentVariables);
  }

  /**
   * Entry point for tools with tool specific behaviour
   *
   * @param baseDir
   *          directory from which analysis will be run
   * @param data
   *          big mess of configuration options
   * @param settings
   *          factory for various strategies. Override default to provide tool
   *          specific behaviours
   */
  public AnalysisResult execute(File baseDir,
                                ReportOptions data,
                                SettingsFactory settings,
                                Map<String, String> environmentVariables) {

    ClassPath classPath = data.getClassPath();

    ProjectClassPaths mutationClassPaths = data.getMutationClassPaths();
    CoverageOptions coverageOptions = settings.createCoverageOptions();
    CodeSource code = new CodeSource(mutationClassPaths, coverageOptions.getPitConfig().testClassIdentifier());

    // workaround for apparent java 1.5 JVM bug . . . might not play nicely
    // with distributed testing
    JavaAgent javaAgent = new JarCreatingJarFinder(new ClassPathByteArraySource(classPath));
    KnownLocationJavaAgentFinder javaAgentFinder = new KnownLocationJavaAgentFinder(javaAgent.getJarLocation().value());
    LaunchOptions launchOptions = new LaunchOptions( javaAgentFinder,settings.getJavaExecutable(),
                                                     data.getJvmArgs(),environmentVariables);
    Timings timings = new Timings();
    CoverageGenerator coverageDatabase = new DefaultCoverageGenerator(baseDir,
                                                                      coverageOptions,
                                                                      launchOptions,
                                                                      code,
                                                                      settings.createCoverageExporter(),
                                                                      timings,
                                                                      !data.isVerbose());

    HistoryStore history = new XStreamHistoryStore(data.createHistoryWriter(),
                                                   data.createHistoryReader());

    MutationResultListenerFactory reportFactory = settings.createListener();
    ResultOutputStrategy reportOutput = settings.getOutputStrategy();
    MutationStrategies strategies = new MutationStrategies( settings.createEngine(),
                                                            history,
                                                            coverageDatabase,
                                                            reportFactory,
                                                            reportOutput);

    MutationCoverage report = new MutationCoverage(strategies,
                                                   baseDir,
                                                   code,
                                                   data,
                                                   settings,
                                                   timings);

    try {
      return AnalysisResult.success(report.runReport());
    } catch (IOException e) {
      return AnalysisResult.fail(e);
    } finally {
      javaAgent.close();
      javaAgentFinder.close();
      data.createHistoryWriter().close();
    }
  }
}
