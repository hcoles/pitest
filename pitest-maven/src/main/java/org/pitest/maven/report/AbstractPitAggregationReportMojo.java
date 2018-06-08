package org.pitest.maven.report;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.plexus.util.FileUtils;
import org.pitest.aggregate.ReportAggregator;
import org.pitest.functional.FCollection;
import org.pitest.maven.DependencyFilter;
import org.pitest.mutationtest.config.DirectoryResultOutputStrategy;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.UndatedReportDirCreationStrategy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

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

  private ReportSourceLocator reportSourceLocator = new ReportSourceLocator();

  /**
   * @return  projects to inspect for report files.
   */
  abstract Collection<MavenProject> findDependencies();

  @Override
  protected void executeReport(final Locale locale)
      throws MavenReportException {
    try {
      final Collection<MavenProject> allProjects = findDependencies();

      final ReportAggregator.Builder reportAggregationBuilder = ReportAggregator
          .builder();

      for (final MavenProject proj : allProjects) {
        addProjectFiles(reportAggregationBuilder, proj);
      }

      final ReportAggregator reportAggregator = reportAggregationBuilder
          .resultOutputStrategy(new DirectoryResultOutputStrategy(
              getReportsDirectory().getAbsolutePath(),
              new UndatedReportDirCreationStrategy()))
          .build();

      reportAggregator.aggregateReport();
    } catch (final Exception e) {
      throw new MavenReportException(e.getMessage(), e);
    }
  }

  private void addProjectFiles(
      final ReportAggregator.Builder reportAggregationBuilder,
      final MavenProject proj) throws IOException, Exception {
    final File projectBaseDir = proj.getBasedir();
    List<File> files = getProjectFilesByFilter(projectBaseDir,
        MUTATION_RESULT_FILTER);
    for (final File file : files) {
      reportAggregationBuilder.addMutationResultsFile(file);
    }
    files = getProjectFilesByFilter(projectBaseDir, LINECOVERAGE_FILTER);
    for (final File file : files) {
      reportAggregationBuilder.addLineCoverageFile(file);
    }
    files = convertToRootDirs(proj.getCompileSourceRoots(),
        proj.getTestCompileSourceRoots());
    for (final File file : files) {
      reportAggregationBuilder.addSourceCodeDirectory(file);
    }
    files = getCompiledDirs(proj);
    for (final File file : files) {
      reportAggregationBuilder.addCompiledCodeDirectory(file);
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private List<File> convertToRootDirs(final List... directoryLists) {
    final List<String> roots = new ArrayList<>();
    for (final List directoryList : directoryLists) {
      roots.addAll(directoryList);
    }
    return FCollection.map(roots, new Function<String, File>() {
      @Override
      public File apply(final String a) {
        return new File(a);
      }
    });
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
  private List<File> getCompiledDirs(final MavenProject project)
      throws Exception {
    final List<String> sourceRoots = new ArrayList<>();
    for (final Object artifactObj : FCollection
        .filter(project.getPluginArtifactMap().values(), new DependencyFilter(
            new PluginServices(this.getClass().getClassLoader())))) {

      final Artifact artifact = (Artifact) artifactObj;
      sourceRoots.add(artifact.getFile().getAbsolutePath());
    }
    return convertToRootDirs(project.getTestClasspathElements(),
        Arrays.asList(project.getBuild().getOutputDirectory(),
            project.getBuild().getTestOutputDirectory()),
        sourceRoots);
  }
}
