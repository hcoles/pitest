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

import java.util.Collection;

import org.pitest.classinfo.ClassName;

public class MutationTestSummaryData implements
    Comparable<MutationTestSummaryData> {

  private final String                fileName;
  private final Collection<String>    mutatedClasses;
  private final Collection<ClassName> testClasses;
    
  private final MutationTotals totals;

  public MutationTestSummaryData(final String fileName,
      final Collection<String> mutatedClasses,
      final Collection<ClassName> testClasses, final MutationTotals totals) {
    this.fileName = fileName;
    this.mutatedClasses = mutatedClasses;
    this.testClasses = testClasses;
    this.totals = totals;

  }
  
  public MutationTotals getTotals() {
    return this.totals;
  }

  public long getNumberOfMutations() {
    return totals.getNumberOfMutations();
  }

  public long getNumberOfMutationsDetected() {
    return totals.getNumberOfMutationsDetected();
  }

  public long getNumberOfLines() {
    return totals.getNumberOfLines();
  }

  public long getNumberOfLinesCovered() {
    return totals.getNumberOfLinesCovered();
  }

  public String getClassName() {
    return this.fileName;
  }

  public String getFileName() {
    return this.fileName.replace('.', '_') + ".html";
  }

  public Collection<String> getMutatedClasses() {
    return this.mutatedClasses;
  }

  public Collection<ClassName> getTestClasses() {
    return this.testClasses;
  }

  public Integer getMutationCoverage() {
    return totals.getMutationCoverage();
  }

  public Integer getLineCoverage() {
    return totals.getLineCoverage();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.getLineCoverage();
    result = prime * result
        + ((this.mutatedClasses == null) ? 0 : this.mutatedClasses.hashCode());
    result = prime * result + getMutationCoverage();
    result = prime * result
        + ((this.testClasses == null) ? 0 : this.testClasses.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MutationTestSummaryData other = (MutationTestSummaryData) obj;
    if (!this.getLineCoverage().equals(other.getLineCoverage())) {
      return false;
    }
    if (this.mutatedClasses == null) {
      if (other.mutatedClasses != null) {
        return false;
      }
    } else if (!this.mutatedClasses.equals(other.mutatedClasses)) {
      return false;
    }
    if (!this.getMutationCoverage().equals(other.getMutationCoverage())) {
      return false;
    }
    if (this.testClasses == null) {
      if (other.testClasses != null) {
        return false;
      }
    } else if (!this.testClasses.equals(other.testClasses)) {
      return false;
    }
    return true;
  }

  public int compareTo(final MutationTestSummaryData other) {
    return this.getFileName().compareTo(other.getFileName());
  }

}