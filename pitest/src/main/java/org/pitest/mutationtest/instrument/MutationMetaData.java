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
import java.util.Set;

import org.pitest.MetaData;
import org.pitest.classinfo.ClassInfo;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.functional.F;
import org.pitest.functional.F2;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.CoverageDatabase;
import org.pitest.mutationtest.report.MutationTestSummaryData;
import org.pitest.mutationtest.report.MutationTotals;
import org.pitest.mutationtest.results.MutationResult;

public class MutationMetaData implements MetaData {

  private final Set<String>             mutatorNames = new HashSet<String>();
  private final Collection<MutationResult> mutations;

  public MutationMetaData(final Collection<String> mutatorNames,
      final Collection<MutationResult> mutations) {
    this.mutations = mutations;
    this.mutatorNames.addAll(mutatorNames);
  }

  public int getNumberOfMutations() {
    return this.mutations.size();
  }

  public String getFirstFileName() {
    return getSourceFiles().iterator().next();
  }

  public Collection<String> getSourceFiles() {
    final Set<String> uniqueFilenames = new HashSet<String>();
    FCollection.mapTo(this.mutations, mutationResultToFileName(),
        uniqueFilenames);
    return uniqueFilenames;
  }

  private F<MutationResult, String> mutationResultToFileName() {

    return new F<MutationResult, String>() {

      public String apply(final MutationResult a) {
        return a.getDetails().getFilename();
      }

    };
  }

  public int getNumberOfDetetectedMutations() {
    int count = 0;
    for (final MutationResult each : this.mutations) {
      if (each.getStatus().isDetected()) {
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

  public Collection<String> getMutatedClass() {
    final Set<String> classes = new HashSet<String>(1);
    FCollection.mapTo(this.mutations, mutationsToClass(), classes);
    return classes;
  }

  private F<MutationResult, String> mutationsToClass() {
    return new F<MutationResult, String>() {
      public String apply(final MutationResult a) {
        return a.getDetails().getClazz();
      }
    };
  }


  public Collection<TestInfo> getTargettedTests() {
    final Set<TestInfo> uniqueTests = new HashSet<TestInfo>();
    FCollection.flatMapTo(this.mutations, mutationToTargettedTests(),
        uniqueTests);
    return uniqueTests;
  }
  
  public String getPackageName() {
    final String fileName = getMutatedClass().iterator()
        .next();
    final int lastDot = fileName.lastIndexOf('.');
    return lastDot > 0 ? fileName.substring(0, lastDot) : "default";
  }
  
  public MutationTestSummaryData createSummaryData(CoverageDatabase coverage) {
    
    final MutationTotals totals = new MutationTotals();
    totals.addClasses(1); // FIXME assumes 1 new top level class per meta data
    totals.addLines(FCollection.fold(accumulateCodeLines(), 0,
        coverage.getClassInfo(getMutatedClass())));
    totals.addLinesCovered(coverage
        .getNumberOfCoveredLines(getMutatedClass()));
    totals.addMutations(getNumberOfMutations());
    totals.addMutationsDetetcted(
        getNumberOfDetetectedMutations());
    
    return  new MutationTestSummaryData(
        getFirstFileName(),
        mutations, this.mutatorNames,
        totals);
  }

  private F2<Integer, ClassInfo, Integer> accumulateCodeLines() {
    return new F2<Integer, ClassInfo, Integer>() {

      public Integer apply(final Integer a, final ClassInfo b) {
        return a + b.getNumberOfCodeLines();
      }

    };
  }
  
  private F<MutationResult, Iterable<TestInfo>> mutationToTargettedTests() {
    return new F<MutationResult, Iterable<TestInfo>>() {

      public Iterable<TestInfo> apply(final MutationResult a) {
        return a.getDetails().getTestsInOrder();
      }

    };
  }

}
