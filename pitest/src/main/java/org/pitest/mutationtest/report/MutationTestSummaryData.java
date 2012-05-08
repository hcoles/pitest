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
package org.pitest.mutationtest.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.pitest.coverage.domain.TestInfo;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.mutationtest.MutationResultList;
import org.pitest.mutationtest.results.MutationResult;

public class MutationTestSummaryData implements
    Comparable<MutationTestSummaryData> {

  private final String                fileName;
  private final Set<String> mutators = new HashSet<String>();
  
  private final Collection<MutationResult> mutations = new ArrayList<MutationResult>();

  private final MutationTotals        totals;

  public MutationTestSummaryData(final String fileName,
      Collection<MutationResult> results,
      final Collection<String> mutators,
      final MutationTotals totals) {
    this.fileName = fileName;
    this.totals = totals;
    this.mutations.addAll( results);
    this.mutators.addAll(mutators);
  }

  public MutationTotals getTotals() {
    return this.totals;
  }

  public long getNumberOfMutations() {
    return this.totals.getNumberOfMutations();
  }

  public long getNumberOfMutationsDetected() {
    return this.totals.getNumberOfMutationsDetected();
  }

  public long getNumberOfLines() {
    return this.totals.getNumberOfLines();
  }

  public long getNumberOfLinesCovered() {
    return this.totals.getNumberOfLinesCovered();
  }

  public String getPackageName() {
    final String fileName = getMutatedClasses().iterator()
        .next();
    final int lastDot = fileName.lastIndexOf('.');
    return lastDot > 0 ? fileName.substring(0, lastDot) : "default";
  }
  
  public String getClassName() {
    return this.fileName;
  }

  public String getFileName() {
    return this.fileName + ".html";
  }

  public Collection<String> getMutatedClasses() {
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


  public Integer getMutationCoverage() {
    return this.totals.getMutationCoverage();
  }

  public Integer getLineCoverage() {
    return this.totals.getLineCoverage();
  }


  public int compareTo(final MutationTestSummaryData other) {
    return this.getFileName().compareTo(other.getFileName());
  }

  public void add(MutationTestSummaryData data) {
    totals.addIgnoringLinesAndClasses(data.getTotals());
    this.mutations.addAll(data.mutations);
    mutators.addAll(data.getMutators());
    
  }

  public Collection<TestInfo> getTests() {
    final Set<TestInfo> uniqueTests = new HashSet<TestInfo>();
    FCollection.flatMapTo(this.mutations, mutationToTargettedTests(),
        uniqueTests);
    return uniqueTests;
  }
  
  private F<MutationResult, Iterable<TestInfo>> mutationToTargettedTests() {
    return new F<MutationResult, Iterable<TestInfo>>() {

      public Iterable<TestInfo> apply(final MutationResult a) {
        return a.getDetails().getTestsInOrder();
      }

    };
  }
  
  public Set<String> getMutators() {
    return mutators;
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
  
  public MutationResultList getResultsForSourceFile(final String sourceFile) {
    return new MutationResultList(FCollection.filter(this.mutations,
        mutationIsForFile(sourceFile)));
  }

  private F<MutationResult, Boolean> mutationIsForFile(final String sourceFile) {
    return new F<MutationResult, Boolean>() {

      public Boolean apply(final MutationResult a) {
        return a.getDetails().getFilename().equals(sourceFile);
      }

    };
  }
  
  
  public Collection<String> getClassesForSourceFile(final String sourceFileName) {
    final Set<String> classes = new HashSet<String>();
    final F<MutationResult, Iterable<String>> f = new F<MutationResult, Iterable<String>>() {
      public Iterable<String> apply(final MutationResult a) {
        if (a.getDetails().getFilename().equals(sourceFileName)) {
          return Option.some(a.getDetails().getClazz());
        } else {
          return Option.none();
        }

      }

    };
    FCollection.flatMapTo(this.mutations, f, classes);
    return classes;
  }


}