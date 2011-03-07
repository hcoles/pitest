/*
 * Copyright 2010 Henry Coles
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
package org.pitest.mutationtest;

import static org.pitest.functional.Prelude.or;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.predicate.True;
import org.pitest.internal.ClassPath;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.instrument.PercentAndConstantTimeoutStrategy;

public class ReportOptions {

  private boolean                                    isValid;

  private Collection<Predicate<String>>              classesInScope;
  private Collection<Predicate<String>>              targetClasses;
  private String                                     reportDir;
  private Collection<File>                           sourceDirs;
  private Collection<String>                         classPathElements;
  private Collection<? extends MethodMutatorFactory> mutators;
  private int                                        dependencyAnalysisMaxDistance;
  private boolean                                    mutateStaticInitializers = true;

  private boolean                                    showHelp;

  private boolean                                    isTestCentric;

  private boolean                                    includeJarFiles          = false;

  private List<String>                               jvmArgs                  = new ArrayList<String>();
  private int                                        numberOfThreads          = 0;
  private float                                      timeoutFactor            = PercentAndConstantTimeoutStrategy.DEFAULT_FACTOR;
  private long                                       timeoutConstant          = PercentAndConstantTimeoutStrategy.DEFAULT_CONSTANT;

  private Collection<Predicate<String>>              targetTests;

  public ReportOptions() {
  }

  public boolean isShowHelp() {
    return this.showHelp;
  }

  public void setShowHelp(final boolean showHelp) {
    this.showHelp = showHelp;
  }

  public Predicate<String> getClassesInScopeFilter() {
    return or(this.classesInScope);
  }

  public void setClassesInScope(
      final Collection<Predicate<String>> classesInScope) {
    this.classesInScope = classesInScope;
  }

  /**
   * @return the reportDir
   */
  public String getReportDir() {
    return this.reportDir;
  }

  /**
   * @param reportDir
   *          the reportDir to set
   */
  public void setReportDir(final String reportDir) {
    this.reportDir = reportDir;
  }

  /**
   * @return the sourceDirs
   */
  public Collection<File> getSourceDirs() {
    return this.sourceDirs;
  }

  public Collection<String> getClassPathElements() {
    return this.classPathElements;
  }

  public void setClassPathElements(final Collection<String> classPathElements) {
    this.classPathElements = classPathElements;
  }

  /**
   * @param sourceDirs
   *          the sourceDirs to set
   */
  public void setSourceDirs(final Collection<File> sourceDirs) {
    this.sourceDirs = sourceDirs;
  }

  /**
   * @return the mutators
   */
  public Collection<? extends MethodMutatorFactory> getMutators() {
    return this.mutators;
  }

  /**
   * @param mutators
   *          the mutators to set
   */
  public void setMutators(
      final Collection<? extends MethodMutatorFactory> mutators) {
    this.mutators = mutators;
  }

  /**
   * @return the dependencyAnalysisMaxDistance
   */
  public int getDependencyAnalysisMaxDistance() {
    return this.dependencyAnalysisMaxDistance;
  }

  /**
   * @param dependencyAnalysisMaxDistance
   *          the dependencyAnalysisMaxDistance to set
   */
  public void setDependencyAnalysisMaxDistance(
      final int dependencyAnalysisMaxDistance) {
    this.dependencyAnalysisMaxDistance = dependencyAnalysisMaxDistance;
  }

  public boolean isValid() {
    return this.isValid;
  }

  public void setValid(final boolean isValid) {
    this.isValid = isValid;
  }

  public boolean shouldShowHelp() {
    return this.showHelp;
  }

  public void setIsTestCentric(final boolean isTestCentric) {
    this.isTestCentric = isTestCentric;
  }

  public boolean isTestCentric() {
    return this.isTestCentric;
  }

  public List<String> getJvmArgs() {
    return this.jvmArgs;
  }

  public void addChildJVMArgs(final List<String> args) {
    this.jvmArgs = args;
  }

  public Option<ClassPath> getClassPath(final boolean declareCaches) {
    if (this.classPathElements != null) {
      return Option.some(createClassPathFromElements(declareCaches));
    } else {
      return Option.<ClassPath> none();
    }
  }

  private ClassPath createClassPathFromElements(final boolean declareCaches) {

    return new ClassPath(
        FCollection.map(this.classPathElements, stringToFile()), true);
  }

  private F<String, File> stringToFile() {
    return new F<String, File>() {

      public File apply(final String a) {
        return new File(a);
      }

    };
  }

  public Collection<Predicate<String>> getTargetClasses() {
    return this.targetClasses;
  }

  public Predicate<String> getTargetClassesFilter() {
    return or(this.targetClasses);
  }

  public void setTargetClasses(final Collection<Predicate<String>> targetClasses) {
    this.targetClasses = targetClasses;
  }

  public void setTargetTests(
      final Collection<Predicate<String>> targetTestsPredicates) {
    this.targetTests = targetTestsPredicates;
  }

  public boolean hasValueForClassesInScope() {
    return (this.classesInScope != null) && !this.classesInScope.isEmpty();
  }

  public boolean isMutateStaticInitializers() {
    return this.mutateStaticInitializers;
  }

  public void setMutateStaticInitializers(final boolean mutateStaticInitializers) {
    this.mutateStaticInitializers = mutateStaticInitializers;
  }

  public int getNumberOfThreads() {
    return this.numberOfThreads;
  }

  public void setNumberOfThreads(final int numberOfThreads) {
    this.numberOfThreads = numberOfThreads;
  }

  public boolean isIncludeJarFiles() {
    return this.includeJarFiles;
  }

  public void setIncludeJarFiles(final boolean includeJarFiles) {
    this.includeJarFiles = includeJarFiles;
  }

  public float getTimeoutFactor() {
    return this.timeoutFactor;
  }

  public long getTimeoutConstant() {
    return this.timeoutConstant;
  }

  public void setTimeoutConstant(final long timeoutConstant) {
    this.timeoutConstant = timeoutConstant;
  }

  public void setTimeoutFactor(final float timeoutFactor) {
    this.timeoutFactor = timeoutFactor;
  }

  public Collection<Predicate<String>> getTargetTests() {
    return this.targetTests;
  }

  @Override
  public String toString() {
    return "ReportOptions [isValid=" + this.isValid + ", classesInScope="
        + this.classesInScope + ", targetClasses=" + this.targetClasses
        + ", reportDir=" + this.reportDir + ", sourceDirs=" + this.sourceDirs
        + ", classPathElements=" + this.classPathElements + ", mutators="
        + this.mutators + ", dependencyAnalysisMaxDistance="
        + this.dependencyAnalysisMaxDistance + ", mutateStaticInitializers="
        + this.mutateStaticInitializers + ", showHelp=" + this.showHelp
        + ", isTestCentric=" + this.isTestCentric + ", includeJarFiles="
        + this.includeJarFiles + ", jvmArgs=" + this.jvmArgs
        + ", numberOfThreads=" + this.numberOfThreads + ", timeoutFactor="
        + this.timeoutFactor + ", timeoutConstant=" + this.timeoutConstant
        + ", targetTests=" + this.targetTests + "]";
  }

  public Predicate<String> getTargetTestsFilter() {
    if ((this.targetTests == null) || this.targetTests.isEmpty()) {
      return True.all();
    } else {
      return or(this.targetTests);
    }

  }

}
