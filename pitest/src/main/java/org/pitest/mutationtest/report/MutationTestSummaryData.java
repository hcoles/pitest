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

public class MutationTestSummaryData implements
    Comparable<MutationTestSummaryData> {

  private final String                fileName;
  private final Collection<String>    mutatedClasses;

  private final MutationTotals        totals;

  public MutationTestSummaryData(final String fileName,
      final Collection<String> mutatedClasses,
      final MutationTotals totals) {
    this.fileName = fileName;
    this.mutatedClasses = mutatedClasses;
    this.totals = totals;

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

  public String getClassName() {
    return this.fileName;
  }

  public String getFileName() {
    return this.fileName + ".html";
  }

  public Collection<String> getMutatedClasses() {
    return this.mutatedClasses;
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

}