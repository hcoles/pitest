package org.pitest.maven.report;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.plexus.util.FileUtils;
import org.pitest.aggregate.AggregationResult;
import org.pitest.aggregate.ReportAggregator;
import org.pitest.functional.FCollection;
import org.pitest.maven.DependencyFilter;
import org.pitest.mutationtest.config.DirectoryResultOutputStrategy;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.UndatedReportDirCreationStrategy;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Common code for report aggregation mojo.
 */
abstract class AbstractPitAggregationReportMojo extends PitReportMojo {

  private static final String REPORT_DIR_RELATIVE_TO_PROJECT = "target/pit-reports";
  private static final String MUTATION_RESULT_FILTER = "mutations.xml";
  private static final String LINECOVERAGE_FILTER = "linecoverage.xml";

  /**
   * The projects in the reactor.
   */
  @Parameter(property = "reactorProjects", readonly = true)
  List<MavenProject> reactorProjects;

  /**
   * Mutation score threshold at which to fail build
   */
  @Parameter(defaultValue = "0", property = "aggregatedMutationThreshold")
  private int aggregatedMutationThreshold;

  /**
   * Test strength score threshold at which to fail build
   */
  @Parameter(defaultValue = "0", property = "aggregatedTestStrengthThreshold")
  private int aggregatedTestStrengthThreshold;

  /**
   * Maximum surviving mutants to allow
   */
  @Parameter(defaultValue = "-1", property = "aggregatedMaxSurviving")
  private int aggregatedMaxSurviving = -1;

  private final ReportSourceLocator reportSourceLocator = new ReportSourceLocator();

  /**
   * @return  projects to inspect for report files.
   */
  abstract Collection<MavenProject> findDependencies();

  @Override
  protected void executeReport(final Locale locale)
      throws MavenReportException {
    if (!canGenerateReport()) {
      getLog().info("Skipping");
      return;
    }

    try {
      final Collection<MavenProject> allProjects = findDependencies();

      final ReportAggregator.Builder reportAggregationBuilder = ReportAggregator
          .builder();

      for (final MavenProject proj : allProjects) {
        addProjectFiles(reportAggregationBuilder, proj);
      }

      final ReportAggregator reportAggregator = reportAggregationBuilder
              .inputCharSet(Charset.forName(this.getInputEncoding()))
              .outputCharset(Charset.forName(this.getOutputEncoding()))
          .resultOutputStrategy(new DirectoryResultOutputStrategy(
              getReportsDirectory().getAbsolutePath(),
              new UndatedReportDirCreationStrategy()))
          .build();

      AggregationResult result = reportAggregator.aggregateReport();

      throwErrorIfTestStrengthBelowThreshold(result.getTestStrength());
      throwErrorIfScoreBelowThreshold(result.getMutationCoverage());
      throwErrorIfMoreThanMaximumSurvivors(result.getMutationsSurvived());

    } catch (final Exception e) {
      throw new MavenReportException(e.getMessage(), e);
    }
  }

  private void addProjectFiles(
      final ReportAggregator.Builder reportAggregationBuilder,
      final MavenProject proj) throws Exception {
    final File projectBaseDir = proj.getBasedir();
    for (final File file : getProjectFilesByFilter(projectBaseDir,
            MUTATION_RESULT_FILTER)) {
      reportAggregationBuilder.addMutationResultsFile(file);
    }

    for (final File file : getProjectFilesByFilter(projectBaseDir, LINECOVERAGE_FILTER)) {
      reportAggregationBuilder.addLineCoverageFile(file);
    }

    for (final File file : convertToRootDirs(proj.getCompileSourceRoots(),
            proj.getTestCompileSourceRoots())) {
      reportAggregationBuilder.addSourceCodeDirectory(file);
    }

    // The kotlin plugin does not add the source dirs to the maven model. Build helper plugin
    // won't trigger if goals called directly. Easiest way to plug this is this hack to
    // always attempt to add the kotlin source dir on its standard location
    reportAggregationBuilder.addSourceCodeDirectory(proj.getBasedir().toPath()
            .resolve("src").resolve("main").resolve("kotlin").toFile());


    for (final File file : getCompiledDirs(proj)) {
      reportAggregationBuilder.addCompiledCodeDirectory(file);
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private List<File> convertToRootDirs(final List... directoryLists) {
    final List<String> roots = new ArrayList<>();
    for (final List directoryList : directoryLists) {
      roots.addAll(directoryList);
    }
    return roots.stream()
            .map(File::new)
            .collect(Collectors.toList());
  }

  private List<File> getProjectFilesByFilter(final File projectBaseDir,
                                             final String filter) throws IOException {

    File reportsDir = projectBaseDir.toPath().resolve(REPORT_DIR_RELATIVE_TO_PROJECT).toFile();
    if (!reportsDir.exists()) {
      return new ArrayList<>();
    }

    File latestReportDir = reportSourceLocator.locate(reportsDir, getLog());

    final List<File> files = FileUtils.getFiles(latestReportDir, filter, "");
    return files == null ? new ArrayList<>() : files;
  }

  @SuppressWarnings("unchecked")
  private List<File> getCompiledDirs(final MavenProject project) {
    final List<String> sourceRoots = new ArrayList<>();
    for (final Object artifactObj : FCollection
        .filter(project.getPluginArtifactMap().values(), new DependencyFilter(
            PluginServices.makeForLoader(this.getClass().getClassLoader())))) {

      final Artifact artifact = (Artifact) artifactObj;
      sourceRoots.add(artifact.getFile().getAbsolutePath());
    }
    return convertToRootDirs(project.getTestCompileSourceRoots(),
        Arrays.asList(project.getBuild().getOutputDirectory(),
            project.getBuild().getTestOutputDirectory()),
        sourceRoots);
  }

  private void throwErrorIfScoreBelowThreshold(final int mutationCoverage)
      throws MojoFailureException {
    if ((this.aggregatedMutationThreshold != 0)
        && (mutationCoverage < this.aggregatedMutationThreshold)) {
      throw new MojoFailureException("Mutation score of "
          + mutationCoverage + " is below threshold of "
          + this.aggregatedMutationThreshold);
    }
  }

  private void throwErrorIfTestStrengthBelowThreshold(final int testStrength)
      throws MojoFailureException {
    if ((this.aggregatedTestStrengthThreshold != 0)
        && (testStrength < this.aggregatedTestStrengthThreshold)) {
      throw new MojoFailureException("Test strength score of "
          + testStrength + " is below threshold of "
          + this.aggregatedTestStrengthThreshold);
    }
  }

  private void throwErrorIfMoreThanMaximumSurvivors(final long mutationsSurvived)
      throws MojoFailureException {
    if ((this.aggregatedMaxSurviving >= 0)
        && (mutationsSurvived > this.aggregatedMaxSurviving)) {
      throw new MojoFailureException("Had "
          + mutationsSurvived + " surviving mutants, but only "
          + this.aggregatedMaxSurviving + " survivors allowed");
    }
  }
}
