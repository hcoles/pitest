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

public class MutationTestSummaryData {

  private final Collection<String> mutatedClasses;
  private final Collection<String> testClasses;
  private final Integer            mutationCoverage;
  private final Integer            lineCoverage;

  public MutationTestSummaryData(final Collection<String> mutatedClasses,
      final Collection<String> testClasses, final Integer mutationCoverage,
      final Integer lineCoverage) {

    this.mutatedClasses = mutatedClasses;
    this.testClasses = testClasses;
    this.mutationCoverage = mutationCoverage;
    this.lineCoverage = lineCoverage;
  }

  public String getFileName() {
    final String mainDrivingClass = this.mutatedClasses.iterator().next();
    final int otherClassCount = this.mutatedClasses.size() - 1;

    if (otherClassCount > 0) {
      return mainDrivingClass + "_and_" + otherClassCount + "_others.html";
    } else {
      return mainDrivingClass + ".html";
    }
  }

  public Collection<String> getMutatedClasses() {
    return this.mutatedClasses;
  }

  public Collection<String> getTestClasses() {
    return this.testClasses;
  }

  public Integer getMutationCoverage() {
    return this.mutationCoverage;
  }

  public Integer getLineCoverage() {
    return this.lineCoverage;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((this.lineCoverage == null) ? 0 : this.lineCoverage.hashCode());
    result = prime * result
        + ((this.mutatedClasses == null) ? 0 : this.mutatedClasses.hashCode());
    result = prime
        * result
        + ((this.mutationCoverage == null) ? 0 : this.mutationCoverage
            .hashCode());
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
    if (this.lineCoverage == null) {
      if (other.lineCoverage != null) {
        return false;
      }
    } else if (!this.lineCoverage.equals(other.lineCoverage)) {
      return false;
    }
    if (this.mutatedClasses == null) {
      if (other.mutatedClasses != null) {
        return false;
      }
    } else if (!this.mutatedClasses.equals(other.mutatedClasses)) {
      return false;
    }
    if (this.mutationCoverage == null) {
      if (other.mutationCoverage != null) {
        return false;
      }
    } else if (!this.mutationCoverage.equals(other.mutationCoverage)) {
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

}
