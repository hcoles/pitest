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
import org.pitest.coverage.domain.TestInfo;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.MutationResultList;
import org.pitest.mutationtest.results.MutationResult;

public class MutationMetaData implements MetaData {

  private final MutationConfig             config;
  private final Collection<MutationResult> mutations;

  public MutationMetaData(final MutationConfig config,
      final Collection<MutationResult> mutations) {
    this.mutations = mutations;
    this.config = config;
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

  public MutationConfig getConfig() {
    return this.config;
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
  

  
  private F<MutationResult, Iterable<TestInfo>> mutationToTargettedTests() {
    return new F<MutationResult, Iterable<TestInfo>>() {

      public Iterable<TestInfo> apply(final MutationResult a) {
        return a.getDetails().getTestsInOrder();
      }

    };
  }

}
