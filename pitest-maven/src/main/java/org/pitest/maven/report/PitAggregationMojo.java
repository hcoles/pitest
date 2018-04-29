package org.pitest.maven.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.plexus.util.FileUtils;
import org.pitest.aggregate.ReportAggregator;
import java.util.function.Function;
import org.pitest.functional.FCollection;
import org.pitest.maven.DependencyFilter;
import org.pitest.mutationtest.config.DirectoryResultOutputStrategy;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.UndatedReportDirCreationStrategy;

/**
 * Goal which aggregates the results of multiple tests into a single result.
 *
 * <p>
 * Based upon Jacoco's ReportAggregateMojo, creates a structured report (HTML,
 * XML, or CSV) from multiple projects. The report is created the all modules
 * this project includes as dependencies.
 * </p>
 *
 * <p>
 * To successfully aggregate reports, each of the individual sub-module reports
 * must have the exportLineCoverage set to <code>true</code>, and must export an
 * XML formatted report. The the developer would simply include an extra module,
 * which has all of the modules which contain reports as dependencies. That
 * "report-aggregation" module would then call this mojo to aggregate all of the
 * individual reports into a single report.
 * </p>
 *
 */
@Mojo(name = "report-aggregate", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class PitAggregationMojo extends PitReportMojo {

  private static final String MUTATION_RESULT_FILTER = "target/pit-reports/mutations.xml";
  private static final String LINECOVERAGE_FILTER    = "target/pit-reports/linecoverage.xml";
  /**
   * The projects in the reactor.
   */
  @Parameter(property = "reactorProjects", readonly = true)
  private List<MavenProject>  reactorProjects;

  @Override
  public String getDescription(final Locale locale) {
    return getName(locale) + " Coverage Report.";
  }

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
    final List<File> files = FileUtils.getFiles(projectBaseDir, filter, "");
    return files == null ? new ArrayList<>() : files;
  }

  @SuppressWarnings("unchecked")
  private List<File> getCompiledDirs(final MavenProject project)
      throws Exception {
    final List<String> sourceRoots = new ArrayList<>();
    for (final Object artifactObj : FCollection
        .filter(project.getPluginArtifactMap().values(), new DependencyFilter(
            new PluginServices(PitAggregationMojo.class.getClassLoader())))) {

      final Artifact artifact = (Artifact) artifactObj;
      sourceRoots.add(artifact.getFile().getAbsolutePath());
    }
    return convertToRootDirs(project.getTestClasspathElements(),
        Arrays.asList(project.getBuild().getOutputDirectory(),
            project.getBuild().getTestOutputDirectory()),
        sourceRoots);
  }

  // this method comes from
  // https://github.com/jacoco/jacoco/blob/master/jacoco-maven-plugin/src/org/jacoco/maven/ReportAggregateMojo.java
  private List<MavenProject> findDependencies() {
    final List<MavenProject> result = new ArrayList<>();
    final List<String> scopeList = Arrays.asList(Artifact.SCOPE_COMPILE,
        Artifact.SCOPE_RUNTIME, Artifact.SCOPE_PROVIDED, Artifact.SCOPE_TEST);
    for (final Object dependencyObject : getProject().getDependencies()) {
      final Dependency dependency = (Dependency) dependencyObject;
      if (scopeList.contains(dependency.getScope())) {
        final MavenProject project = findProjectFromReactor(dependency);
        if (project != null) {
          result.add(project);
        }
      }
    }
    return result;
  }

  // this method comes from
  // https://github.com/jacoco/jacoco/blob/master/jacoco-maven-plugin/src/org/jacoco/maven/ReportAggregateMojo.java
  private MavenProject findProjectFromReactor(final Dependency d) {
    for (final MavenProject p : reactorProjects) {
      if (p.getGroupId().equals(d.getGroupId())
          && p.getArtifactId().equals(d.getArtifactId())
          && p.getVersion().equals(d.getVersion())) {
        return p;
      }
    }
    return null;
  }

}
