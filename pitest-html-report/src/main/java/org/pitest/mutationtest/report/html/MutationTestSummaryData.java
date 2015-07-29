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
package org.pitest.mutationtest.report.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.pitest.classinfo.ClassInfo;
import org.pitest.coverage.TestInfo;
import org.pitest.functional.F;
import org.pitest.functional.F2;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.MutationResult;

public class MutationTestSummaryData {

  private final String                     fileName;
  private final Set<String>                mutators  = new HashSet<String>();
  private final Collection<MutationResult> mutations = new ArrayList<MutationResult>();
  private final Set<ClassInfo>             classes   = new HashSet<ClassInfo>();

  private long                             numberOfCoveredLines;

  public MutationTestSummaryData(final String fileName,
      final Collection<MutationResult> results,
      final Collection<String> mutators, final Collection<ClassInfo> classes,
      final long numberOfCoveredLines) {
    this.fileName = fileName;
    this.mutations.addAll(results);
    this.mutators.addAll(mutators);
    this.classes.addAll(classes);
    this.numberOfCoveredLines = numberOfCoveredLines;
  }

  public MutationTotals getTotals() {
    final MutationTotals mt = new MutationTotals();
    mt.addFiles(1);
    mt.addMutations(this.getNumberOfMutations());
    mt.addMutationsDetetcted(this.getNumberOfMutationsDetected());
    mt.addLines(getNumberOfLines());
    mt.addLinesCovered(this.numberOfCoveredLines);
    return mt;
  }

  public String getPackageName() {
    final String packageName = getMutatedClasses().iterator().next().getName()
        .asJavaName();
    final int lastDot = packageName.lastIndexOf('.');
    return lastDot > 0 ? packageName.substring(0, lastDot) : "default";
  }

  public void add(final MutationTestSummaryData data) {
    this.mutations.addAll(data.mutations);
    this.mutators.addAll(data.getMutators());
    final int classesBefore = this.classes.size();
    this.classes.addAll(data.classes);
    if (classesBefore < this.classes.size()) {
      this.numberOfCoveredLines += data.numberOfCoveredLines;
    }
  }

  public Collection<TestInfo> getTests() {
    final Set<TestInfo> uniqueTests = new HashSet<TestInfo>();
    FCollection.flatMapTo(this.mutations, mutationToTargettedTests(),
        uniqueTests);
    return uniqueTests;
  }

  public String getFileName() {
    return this.fileName;
  }

  public Collection<ClassInfo> getMutatedClasses() {
    return this.classes;
  }

  public Set<String> getMutators() {
    return this.mutators;
  }

  public MutationResultList getResults() {
    return new MutationResultList(this.mutations);
  }

  public Collection<ClassInfo> getClasses() {
    return this.classes;
  }

  private int getNumberOfLines() {
    return FCollection.fold(accumulateCodeLines(), 0, this.classes);
  }

  private F2<Integer, ClassInfo, Integer> accumulateCodeLines() {
    return new F2<Integer, ClassInfo, Integer>() {

      @Override
      public Integer apply(final Integer a, final ClassInfo b) {
        return a + b.getNumberOfCodeLines();
      }

    };
  }

  private long getNumberOfMutations() {
    return this.mutations.size();
  }

  private long getNumberOfMutationsDetected() {
    int count = 0;
    for (final MutationResult each : this.mutations) {
      if (each.getStatus().isDetected()) {
        count++;
      }
    }
    return count;
  }

  private F<MutationResult, Iterable<TestInfo>> mutationToTargettedTests() {
    return new F<MutationResult, Iterable<TestInfo>>() {

      @Override
      public Iterable<TestInfo> apply(final MutationResult a) {
        return a.getDetails().getTestsInOrder();
      }

    };
  }

}