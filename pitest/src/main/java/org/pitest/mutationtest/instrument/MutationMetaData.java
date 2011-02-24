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
package org.pitest.mutationtest.instrument;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pitest.MetaData;
import org.pitest.classinfo.ClassInfo;
import org.pitest.extension.TestUnit;
import org.pitest.functional.F;
import org.pitest.functional.F2;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.MutationResultList;
import org.pitest.mutationtest.instrument.ResultsReader.MutationResult;
import org.pitest.mutationtest.report.MutationTestSummaryData;

public class MutationMetaData implements MetaData {

  private final MutationConfig             config;
  private final Collection<ClassInfo>      mutatedClasses;
  private final Collection<MutationResult> mutations;
  private final Option<Statistics>         stats;

  protected MutationMetaData(final MutationConfig config,
      final Collection<ClassInfo> mutatedClasses,
      final Option<Statistics> stats, final Collection<MutationResult> mutations) {
    this.mutations = mutations;
    this.mutatedClasses = mutatedClasses;
    this.stats = stats;
    this.config = config;
  }

  public int getNumberOfMutations() {
    return this.mutations.size();
  }

  public Collection<String> getSourceFiles() {

    final Set<String> uniqueFilenames = new HashSet<String>();
    FCollection
        .map(this.mutations, mutationResultToFileName(), uniqueFilenames);
    return uniqueFilenames;

  }

  private F<MutationResult, String> mutationResultToFileName() {

    return new F<MutationResult, String>() {

      public String apply(final MutationResult a) {
        return a.details.getFilename();
      }

    };
  }

  public Collection<String> getTestClasses() {
    final Statistics stats = this.stats.value();
    final Set<String> uniqueTestClasses = new HashSet<String>();
    final F2<Set<String>, TestUnit, Set<String>> f = new F2<Set<String>, TestUnit, Set<String>>() {
      public Set<String> apply(final Set<String> a, final TestUnit b) {
        a.addAll(b.getDescription().getTestClassNames());
        return a;
      }
    };

    return FCollection.fold(f, uniqueTestClasses, stats.getAllTests());
  }

  public MutationTestSummaryData getSummaryData() {
    return new MutationTestSummaryData(this.config.getRunType(),
        this.mutatedClasses, getTestClasses(),
        this.getPercentageMutationCoverage(), getPercentageLineCoverage());
  }

  public int getPercentageLineCoverage() {
    final Statistics stats = this.stats.value();
    return Math.round(100f / getNumberOfCodeLines()
        * stats.getNumberOfLinesWithCoverage());
  }

  private float getNumberOfCodeLines() {
    return FCollection.fold(accumulateCodeLines(), 0, this.mutatedClasses);
  }

  private F2<Integer, ClassInfo, Integer> accumulateCodeLines() {
    // TODO Auto-generated method stub
    return new F2<Integer, ClassInfo, Integer>() {

      public Integer apply(final Integer a, final ClassInfo b) {
        return a + b.getCodeLines().size();
      }

    };
  }

  public int getNumberOfDetetectedMutations() {
    int count = 0;
    for (final MutationResult each : this.mutations) {
      if (each.status.isDetected()) {
        count++;
      }
    }
    return count;
  }

  public int getPercentageMutationCoverage() {
    if (getNumberOfMutations() != 0) {
      return Math.round(100f / getNumberOfMutations()
          * getNumberOfDetetectedMutations());
    } else {
      return 100;
    }
  }

  public Collection<MutationResult> getMutations() {
    return this.mutations;
  }

  public Collection<ClassInfo> getMutatedClass() {
    return this.mutatedClasses;
  }

  public Option<Statistics> getStats() {
    return this.stats;
  }

  public MutationConfig getConfig() {
    return this.config;
  }

  public MutationResultList getResultsForSourceFile(final String sourceFile) {
    return new MutationResultList(FCollection.filter(this.mutations,
        mutationIsForFile(sourceFile)));
  }

  private F<MutationResult, Boolean> mutationIsForFile(final String sourceFile) {
    // TODO Auto-generated method stub
    return new F<MutationResult, Boolean>() {

      public Boolean apply(final MutationResult a) {
        return a.details.getFilename().equals(sourceFile);
      }

    };
  }

  public Collection<ClassInfo> getClassesForSourceFile(
      final String sourceFileName) {
    final Set<ClassInfo> classes = new HashSet<ClassInfo>();
    final F<MutationResult, Iterable<ClassInfo>> f = new F<MutationResult, Iterable<ClassInfo>>() {
      public Iterable<ClassInfo> apply(final MutationResult a) {
        if (a.details.getFilename().equals(sourceFileName)) {
          return getClassInfo(a.details.getClazz());
        } else {
          return Option.none();
        }

      }

    };
    FCollection.flatMap(this.mutations, f, classes);
    return classes;
  }

  private List<ClassInfo> getClassInfo(final String clazz) {
    return FCollection.filter(this.mutatedClasses, nameIs(clazz));
  }

  private F<ClassInfo, Boolean> nameIs(final String clazz) {
    return new F<ClassInfo, Boolean>() {

      public Boolean apply(final ClassInfo a) {

        return a.getName().equals(clazz);
      }

    };
  }

}
