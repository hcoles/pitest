package org.pitest.maven.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

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
public class PitAggregationMojo extends AbstractPitAggregationReportMojo {

  @Override
  public String getDescription(final Locale locale) {
    return getName(locale) + " Coverage Report.";
  }

  // this method comes from
  // https://github.com/jacoco/jacoco/blob/master/jacoco-maven-plugin/src/org/jacoco/maven/ReportAggregateMojo.java
  @Override
  List<MavenProject> findDependencies() {
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
