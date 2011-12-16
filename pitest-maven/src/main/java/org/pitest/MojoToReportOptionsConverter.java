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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.Mutator;
import org.pitest.mutationtest.MutatorGrouping;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.report.OutputFormat;
import org.pitest.util.Functions;
import org.pitest.util.Glob;

public class MojoToReportOptionsConverter {

  private final PitMojo mojo;

  public MojoToReportOptionsConverter(final PitMojo mojo) {
    this.mojo = mojo;
  }

  @SuppressWarnings("unchecked")
  public ReportOptions convert() {

    final Set<String> classPath = new HashSet<String>();

    try {
      classPath.addAll(this.mojo.getProject().getTestClasspathElements());
      classPath.addAll(this.mojo.getProject().getCompileClasspathElements());
      classPath.addAll(this.mojo.getProject().getRuntimeClasspathElements());
      classPath.addAll(this.mojo.getProject().getSystemClasspathElements());

    } catch (final DependencyResolutionRequiredException e1) {
      this.mojo.getLog().info(e1);
      e1.printStackTrace();
    }

    addOwnDependenciesToClassPath(classPath);

    return parseReportOptions(classPath);

  }

  @SuppressWarnings("unchecked")
  private ReportOptions parseReportOptions(final Set<String> classPath) {
    final ReportOptions data = new ReportOptions();
    data.setClassPathElements(classPath);
    data.setDependencyAnalysisMaxDistance(this.mojo.getMaxDependencyDistance());
    data.setIncludeJarFiles(this.mojo.isIncludeJarFiles());

    data.setTargetClasses(determineTargetClasses());
    data.setTargetTests(determineTargetTests());
    data.setClassesInScope(determineClassesInScope());
    data.setMutateStaticInitializers(this.mojo.isMutateStaticInitializers());
    data.setExcludedMethods(globStringsToPredicates(this.mojo
        .getExcludedMethods()));
    data.setExcludedClasses(globStringsToPredicates(this.mojo
        .getExcludedClasses()));
    data.setNumberOfThreads(this.mojo.getThreads());
    data.setMaxMutationsPerClass(this.mojo.getMaxMutationsPerClass());

    data.setReportDir(this.mojo.getReportsDirectory().getAbsolutePath());
    data.setVerbose(this.mojo.isVerbose());
    if (this.mojo.getJvmArgs() != null) {
      data.addChildJVMArgs(this.mojo.getJvmArgs());
    }

    data.setMutators(determineMutators());
    data.setTimeoutConstant(this.mojo.getTimeoutConstant());
    data.setTimeoutFactor(this.mojo.getTimeoutFactor());
    if (this.mojo.getAvoidCallsTo() != null) {
      data.setLoggingClasses(this.mojo.getAvoidCallsTo());
    }

    final List<String> sourceRoots = new ArrayList<String>();
    sourceRoots.addAll(this.mojo.getProject().getCompileSourceRoots());
    sourceRoots.addAll(this.mojo.getProject().getTestCompileSourceRoots());

    data.setSourceDirs(stringsTofiles(sourceRoots));

    data.addOutputFormats(determineOutputFormats());

    return data;
  }

  private void addOwnDependenciesToClassPath(final Set<String> classPath) {
    for (final Artifact dependency : filteredDependencies()) {
      classPath.add(dependency.getFile().getAbsolutePath());
    }
  }

  private Collection<Predicate<String>> globStringsToPredicates(
      final List<String> excludedMethods) {
    return FCollection.map(excludedMethods, Glob.toGlobPredicate());
  }

  private Collection<Predicate<String>> determineTargetTests() {
    return FCollection.map(this.mojo.getTargetTests(), Glob.toGlobPredicate());
  }

  private Collection<Artifact> filteredDependencies() {
    final DependencyFilter filter = new DependencyFilter("org.pitest");
    return FCollection
        .filter(this.mojo.getPluginArtifactMap().values(), filter);
  }

  private Collection<MethodMutatorFactory> determineMutators() {
    if (this.mojo.getMutators() != null) {
      return FCollection.flatMap(this.mojo.getMutators(), stringToMutators());
    } else {
      return Mutator.DEFAULTS.asCollection();
    }
  }

  private F<String, MutatorGrouping> stringToMutators() {
    return new F<String, MutatorGrouping>() {
      public Mutator apply(final String a) {
        return Mutator.valueOf(a);
      }

    };
  }

  private Collection<Predicate<String>> determineClassesInScope() {
    return returnOrDefaultToClassesLikeGroupName(this.mojo.getInScopeClasses());
  }

  private Collection<Predicate<String>> determineTargetClasses() {
    return returnOrDefaultToClassesLikeGroupName(this.mojo.getTargetClasses());
  }

  private Collection<Predicate<String>> returnOrDefaultToClassesLikeGroupName(
      final Collection<String> filters) {
    if (filters == null) {
      return Collections.<Predicate<String>> singleton(new Glob(this.mojo
          .getProject().getGroupId() + "*"));
    } else {
      return FCollection.map(filters, Glob.toGlobPredicate());
    }
  }

  private Collection<File> stringsTofiles(final List<String> sourceRoots) {
    return FCollection.map(sourceRoots, stringToFile());
  }

  private F<String, File> stringToFile() {
    return new F<String, File>() {
      public File apply(final String a) {
        return new File(a);
      }

    };
  }

  private Collection<OutputFormat> determineOutputFormats() {
    if (this.mojo.getOutputFormats() != null) {
      return FCollection.map(this.mojo.getOutputFormats(),
          Functions.stringToEnum(OutputFormat.class));
    } else {
      return Arrays.asList(OutputFormat.HTML);
    }
  }

}
