package org.pitest.mutationtest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;

import org.junit.Before;
import org.pitest.classpath.CodeSource;
import org.pitest.classpath.DefaultCodeSource;
import org.pitest.classpath.PathFilter;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.execute.DefaultCoverageGenerator;
import org.pitest.coverage.export.NullCoverageExporter;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.mutationtest.config.TestPluginArguments;
import org.pitest.mutationtest.engine.gregor.config.GregorEngineFactory;
import org.pitest.mutationtest.incremental.NullHistory;
import org.pitest.mutationtest.tooling.JarCreatingJarFinder;
import org.pitest.mutationtest.tooling.MutationCoverage;
import org.pitest.mutationtest.tooling.MutationStrategies;
import org.pitest.mutationtest.verify.NoVerification;
import org.pitest.process.DefaultJavaExecutableLocator;
import org.pitest.process.JavaAgent;
import org.pitest.process.LaunchOptions;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.Glob;
import org.pitest.util.Timings;
import org.pitest.util.Unchecked;
import org.pitest.util.Verbosity;

public abstract class ReportTestBase {

  protected MetaDataExtractor metaDataExtractor;
  protected ReportOptions     data;

  private PluginServices      plugins;

  @Before
  public void setUp() {
    this.metaDataExtractor = new MetaDataExtractor();
    this.plugins = PluginServices.makeForContextLoader();
    this.data = new ReportOptions();
    this.data.setSourceDirs(Collections.emptyList());
    this.data.setGroupConfig(new TestGroupConfig());
  }

  protected MutationResultListenerFactory listenerFactory() {
    return new MutationResultListenerFactory() {
      @Override
      public MutationResultListener getListener(Properties props,
          ListenerArguments args) {
        return ReportTestBase.this.metaDataExtractor;
      }

      @Override
      public String name() {
        return null;
      }

      @Override
      public String description() {
        return null;
      }

    };
  }

  protected void verifyResults(final DetectionStatus... detectionStatus) {
    final List<DetectionStatus> expected = Arrays.asList(detectionStatus);
    final List<DetectionStatus> actual = this.metaDataExtractor
        .getDetectionStatus();

    Collections.sort(expected);
    Collections.sort(actual);

    assertEquals(expected, actual);
  }

  protected Collection<Predicate<String>> predicateFor(final String... glob) {
    return Glob.toGlobPredicates(Arrays.asList(glob));
  }

  protected Collection<Predicate<String>> predicateFor(final Class<?> clazz) {
    return predicateFor(clazz.getName());
  }

  protected void createAndRun() {
    final SettingsFactory settings = new SettingsFactory(this.data, this.plugins);
    createAndRun(settings);
  }

  protected void createAndRun(SettingsFactory settings) {
    final JavaAgent agent = new JarCreatingJarFinder();
    try {

      final CoverageOptions coverageOptions = createCoverageOptions(settings.createCoverageOptions().getPitConfig());
      final LaunchOptions launchOptions = new LaunchOptions(agent,
          new DefaultJavaExecutableLocator(), this.data.getJvmArgs(),
          new HashMap<>());

      final PathFilter pf = new PathFilter(p -> true, p -> true);
      final ProjectClassPaths cps = new ProjectClassPaths(
          this.data.getClassPath(), this.data.createClassesFilter(), pf);

      final Timings timings = new Timings();
      final CodeSource code = new DefaultCodeSource(cps);

      final CoverageGenerator coverageDatabase = new DefaultCoverageGenerator(
          null, coverageOptions, launchOptions, code,
          new NullCoverageExporter(), timings, Verbosity.DEFAULT);

      final History history = new NullHistory();

      final MutationStrategies strategies = new MutationStrategies(
          new GregorEngineFactory(), history, coverageDatabase,
          listenerFactory(), result -> result, cov -> cov, null, new NoVerification());

      final MutationCoverage testee = new MutationCoverage(strategies, null,
          code, this.data, new SettingsFactory(this.data, this.plugins),
          timings);

      testee.runReport();
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    } finally {
      agent.close();
    }
  }

  private CoverageOptions createCoverageOptions(TestPluginArguments configuration) {
    return new CoverageOptions(this.data.getTargetClasses(),this.data.getExcludedClasses(),
        configuration, this.data.getVerbosity());
  }

  protected void setMutators(final String... mutator) {
    this.data.setMutators(Arrays.asList(mutator));
  }

}
