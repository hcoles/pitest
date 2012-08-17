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
import org.pitest.classinfo.ClassName;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.report.MutationTestSummaryData;
import org.pitest.mutationtest.results.MutationResult;

public class MutationMetaData implements MetaData {

  private final Set<String>                mutatorNames = new HashSet<String>();
  private final Collection<MutationResult> mutations;

  public MutationMetaData(final Collection<String> mutatorNames,
      final Collection<MutationResult> mutations) {
    this.mutations = mutations;
    this.mutatorNames.addAll(mutatorNames);
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

  private static F<MutationResult, String> mutationResultToFileName() {
    return new F<MutationResult, String>() {
      public String apply(final MutationResult a) {
        return a.getDetails().getFilename();
      }
    };
  }

  public Collection<MutationResult> getMutations() {
    return this.mutations;
  }

  public Collection<ClassName> getMutatedClass() {
    final Set<ClassName> classes = new HashSet<ClassName>(1);
    FCollection.mapTo(this.mutations, mutationsToClass(), classes);
    return classes;
  }

  private static F<MutationResult, ClassName> mutationsToClass() {
    return new F<MutationResult, ClassName>() {
      public ClassName apply(final MutationResult a) {
        return ClassName.fromString(a.getDetails().getClazz());
      }
    };
  }

  public String getPackageName() {
    final ClassName fileName = getMutatedClass().iterator().next();
    final int lastDot = fileName.asJavaName().lastIndexOf('.');
    return lastDot > 0 ? fileName.asJavaName().substring(0, lastDot) : "default";
  }

  public MutationTestSummaryData createSummaryData(
      final CoverageDatabase coverage) {

    return new MutationTestSummaryData(getFirstFileName(), this.mutations,
        this.mutatorNames, coverage.getClassInfo(getMutatedClass()),
        coverage.getNumberOfCoveredLines(getMutatedClass()));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((mutations == null) ? 0 : mutations.hashCode());
    result = prime * result
        + ((mutatorNames == null) ? 0 : mutatorNames.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MutationMetaData other = (MutationMetaData) obj;
    if (mutations == null) {
      if (other.mutations != null)
        return false;
    } else if (!mutations.equals(other.mutations))
      return false;
    if (mutatorNames == null) {
      if (other.mutatorNames != null)
        return false;
    } else if (!mutatorNames.equals(other.mutatorNames))
      return false;
    return true;
  }

  
  
}
