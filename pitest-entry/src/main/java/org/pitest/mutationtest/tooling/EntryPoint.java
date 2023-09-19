package org.pitest.mutationtest.tooling;

import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassPathByteArraySource;
import org.pitest.classpath.CodeSource;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.execute.DefaultCoverageGenerator;
import org.pitest.mutationtest.History;
import org.pitest.mutationtest.HistoryFactory;
import org.pitest.mutationtest.HistoryParams;
import org.pitest.mutationtest.incremental.HistoryResultInterceptor;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.mutationtest.incremental.NullHistory;
import org.pitest.mutationtest.incremental.NullWriterFactory;
import org.pitest.mutationtest.incremental.WriterFactory;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureParameter;
import org.pitest.plugin.FeatureParser;
import org.pitest.plugin.FeatureSelector;
import org.pitest.process.ArgLineParser;
import org.pitest.process.JavaAgent;
import org.pitest.process.LaunchOptions;
import org.pitest.util.Log;
import org.pitest.util.PitError;
import org.pitest.util.ResultOutputStrategy;
import org.pitest.util.Timings;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;
import static org.pitest.util.Verbosity.VERBOSE;
import static org.pitest.util.Verbosity.VERBOSE_NO_SPINNER;

public class EntryPoint {

  /**
   * Convenient entry point for tools to run mutation analysis.
   *
   * The big grab bag of config stored in ReportOptions must be setup correctly
   * first.
   *
   * @param baseDir
   *          directory from which analysis will be run
   * @param data
   * @param environmentVariables
   *
   */
  public AnalysisResult execute(File baseDir, ReportOptions data,
      PluginServices plugins, Map<String, String> environmentVariables) {
    final SettingsFactory settings = new SettingsFactory(data, plugins);
    return execute(baseDir, data, settings, environmentVariables);
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
  public AnalysisResult execute(File baseDir, ReportOptions data,
      SettingsFactory settings, Map<String, String> environmentVariables) {

    updateData(data, settings);

    if ((data.getVerbosity() == VERBOSE) || (data.getVerbosity() == VERBOSE_NO_SPINNER)) {
      Log.getLogger().info("Project base directory is " + data.getProjectBase());
      Log.getLogger().info("---------------------------------------------------------------------------");
      Log.getLogger().info("Enabled (+) and disabled (-) features.");
      Log.getLogger().info("-----------------------------------------");
      settings.describeFeatures(asInfo("+"), asInfo("-"));
      Log.getLogger().info("---------------------------------------------------------------------------");
    }
    settings.checkRequestedFeatures();

    checkMatrixMode(data);

    final ClassPath cp = data.getClassPath();

    // workaround for apparent java 1.5 JVM bug . . . might not play nicely
    // with distributed testing
    final JavaAgent jac = new JarCreatingJarFinder(
        new ClassPathByteArraySource(cp));

    final KnownLocationJavaAgentFinder ja = new KnownLocationJavaAgentFinder(
        jac.getJarLocation().get());

    final ResultOutputStrategy reportOutput = settings.getOutputStrategy();

    final MutationResultListenerFactory reportFactory = settings
        .createListener();

    final CoverageOptions coverageOptions = settings.createCoverageOptions();
    final LaunchOptions launchOptions = new LaunchOptions(ja,
        settings.getJavaExecutable(), createJvmArgs(data), environmentVariables)
        .usingClassPathJar(data.useClasspathJar());
    final ProjectClassPaths cps = data.getMutationClassPaths();

    final CodeSource code = settings.createCodeSource(cps);

    final Timings timings = new Timings();
    final CoverageGenerator coverageDatabase = new DefaultCoverageGenerator(
        baseDir, coverageOptions, launchOptions, code,
        settings.createCoverageExporter(), timings, data.getVerbosity());

    final Optional<WriterFactory> maybeWriter = data.createHistoryWriter();
    WriterFactory historyWriter = maybeWriter.orElse(new NullWriterFactory());
    HistoryFactory historyFactory = settings.createHistory();
    final History history = pickHistoryStore(code, data, maybeWriter, historyFactory);

    final MutationStrategies strategies = new MutationStrategies(
        settings.createEngine(), history, coverageDatabase, reportFactory, settings.getResultInterceptor().add(new HistoryResultInterceptor(history)),
        settings.createCoverageTransformer(code),
            reportOutput, settings.createVerifier().create(code));

    final MutationCoverage report = new MutationCoverage(strategies, baseDir,
        code, data, settings, timings);

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

  private List<String> createJvmArgs(ReportOptions data) {
    List<String> args = new ArrayList<>(data.getJvmArgs());
    args.addAll(ArgLineParser.split(data.getArgLine()));
    return args;
  }

  private void updateData(ReportOptions data, SettingsFactory settings) {
    settings.createUpdater().updateConfig(null, data);
  }

  private History pickHistoryStore(CodeSource code, ReportOptions data, Optional<WriterFactory> historyWriter, HistoryFactory factory) {
    final Optional<Reader> reader = data.createHistoryReader();
    if (!reader.isPresent() && !historyWriter.isPresent()) {
      return new NullHistory();
    }
    FeatureParser parser = new FeatureParser();
    FeatureSelector select = new FeatureSelector(parser.parseFeatures(data.getFeatures()), singletonList(factory));
    return factory.makeHistory(new HistoryParams(select, code), historyWriter.orElse(new NullWriterFactory()), reader);
  }

  private void checkMatrixMode(ReportOptions data) {
    if (data.isFullMutationMatrix() && !data.getOutputFormats().contains("XML")) {
      throw new PitError("Full mutation matrix is only supported in the output format XML.");
    }
  }

  private Consumer<Feature> asInfo(final String leader) {
    return a -> {
      Log.getLogger().info(String.format("%1$-16s",leader + a.name()) + a.description());
      for (final FeatureParameter each : a.params()) {
        Log.getLogger().info(String.format("%1$-18s", "  [" + each.name() + "]") + each.description());
      }
    };
  }

}
