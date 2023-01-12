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
import org.pitest.mutationtest.incremental.NullHistoryStore;
import org.pitest.mutationtest.incremental.NullWriterFactory;
import org.pitest.mutationtest.incremental.ObjectOutputStreamHistoryStore;
import org.pitest.mutationtest.incremental.WriterFactory;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureParameter;
import org.pitest.process.ArgLineParser;
import org.pitest.process.JavaAgent;
import org.pitest.process.LaunchOptions;
import org.pitest.util.Log;
import org.pitest.util.PitError;
import org.pitest.util.ResultOutputStrategy;
import org.pitest.util.Timings;
import org.pitest.mutationtest.config.TestClassPathPredicate;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.Collection;
import org.pitest.mutationtest.tdg.Tdgimpl;
import org.pitest.mutationtest.tdg.execute.TdgTestMethodNamesGenerator;
import static org.pitest.util.Verbosity.VERBOSE;
import java.util.stream.Collectors;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.tdghistory.NullTdgHistoryStore;
import org.pitest.mutationtest.tdghistory.TdgHistoryStore;
import org.pitest.mutationtest.tdghistory.TdgHistoryStoreImpl;
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
Log.getLogger().info("breakpoint1");
    if (data.getVerbosity() == VERBOSE) {
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
    // Tdgimpl tdg = new Tdgimpl(cps);
    // // Log.getLogger().info("tdg.getDepsMap()" + tdg.getDepsMap());
    // tdg.init();
    // for (ClassName tar : cps.code().stream().collect(Collectors.toList())) {
    //   tdg.getTests(tar);
    // }
    
      
    final CodeSource code = new CodeSource(cps);
    final Timings timings = new Timings();
    TdgTestMethodNamesGenerator classTestMethodNamesGen = 
    new TdgTestMethodNamesGenerator(launchOptions, code, baseDir);
    timings.registerStart(Timings.Stage.TDG_MINION);
    Map<String, Set<String>> classTestMethodNames = classTestMethodNamesGen.getClassMethodNames();
    timings.registerEnd(Timings.Stage.TDG_MINION);
    Tdgimpl tdg = new Tdgimpl(cps, classTestMethodNames);
    // tdg.init();
    // System.out.println(classTestMethodNames);
    // System.out.println(tdg.getTests(ClassName.fromString("org.zipeng.B")));
    
    final CoverageGenerator coverageDatabase = new DefaultCoverageGenerator(
        baseDir, coverageOptions, launchOptions, code,
        settings.createCoverageExporter(), timings, data.getVerbosity());


    final Optional<WriterFactory> maybeWriter = data.createHistoryWriter();
    WriterFactory historyWriter = maybeWriter.orElse(new NullWriterFactory());
    final HistoryStore history = makeHistoryStore(data, maybeWriter);


    final Optional<WriterFactory> tdgmaybeWriter = data.createTdgHistoryWriter();
    WriterFactory tdghistoryWriter = tdgmaybeWriter.orElse(new NullWriterFactory());
    TdgHistoryStore tdgHistory = this.makeTdgHistory(data,tdgmaybeWriter, cps);
    
    // System.out.println("getHistoricResultsgetHistoricResultsgetHistoricResults : "+tdgHistory.getHistoricResults());
    final MutationStrategies strategies = new MutationStrategies(
        settings.createEngine(), history, coverageDatabase, reportFactory,
        reportOutput, tdg, tdgHistory);

    final MutationCoverage report = new MutationCoverage(strategies, baseDir,
        code, data, settings, timings);

    try {
      return AnalysisResult.success(report.runReport());
    } catch (final IOException e) {
      return AnalysisResult.fail(e);
    } finally {
      jac.close();
      ja.close();
      tdghistoryWriter.close();
      historyWriter.close();
    }

  }
  public TdgHistoryStore makeTdgHistory (ReportOptions data, Optional<WriterFactory> historyWriter, ProjectClassPaths classPath) {
    final Optional<Reader> reader = data.createTdgHistoryReader();
    if (!reader.isPresent() && !historyWriter.isPresent()) {
      System.out.println("tdg history file null");
      return new NullTdgHistoryStore();
    }
    // System.out.println("new new tdg history file null");
    return new TdgHistoryStoreImpl(historyWriter.orElse(new NullWriterFactory()), reader, classPath);
  }
  private List<String> createJvmArgs(ReportOptions data) {
    List<String> args = new ArrayList<>(data.getJvmArgs());
    args.addAll(ArgLineParser.split(data.getArgLine()));
    return args;
  }

  private void updateData(ReportOptions data, SettingsFactory settings) {
    settings.createUpdater().updateConfig(null, data);

  }

  private HistoryStore makeHistoryStore(ReportOptions data,  Optional<WriterFactory> historyWriter) {
    final Optional<Reader> reader = data.createHistoryReader();
    if (!reader.isPresent() && !historyWriter.isPresent()) {
      return new NullHistoryStore();
    }
    return new ObjectOutputStreamHistoryStore(historyWriter.orElse(new NullWriterFactory()), reader);
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
