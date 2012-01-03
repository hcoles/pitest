/*
 * Copyright 2011 Henry Coles
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
package org.pitest.mutationtest.statistics;

import org.pitest.Description;
import org.pitest.TestResult;
import org.pitest.extension.TestListener;
import org.pitest.functional.Option;
import org.pitest.mutationtest.instrument.MutationMetaData;

public class MutationStatisticsListener implements TestListener {

  private final MutationStatistics mutatorScores = new MutationStatistics();

  public void onTestError(final TestResult tr) {
    extractMetaData(tr);
  }

  public void onTestFailure(final TestResult tr) {
    extractMetaData(tr);
  }

  public void onTestSkipped(final TestResult tr) {
    extractMetaData(tr);
  }

  public void onTestStart(final Description d) {

  }

  public void onTestSuccess(final TestResult tr) {
    extractMetaData(tr);
  }

  private void extractMetaData(final TestResult tr) {
    final Option<MutationMetaData> d = tr.getValue(MutationMetaData.class);
    if (d.hasSome()) {
      processMetaData(d.value());
    }
  }

  private void processMetaData(final MutationMetaData value) {
    this.mutatorScores.registerResults(value.getMutations());
  }

  public void onRunStart() {
  }

  public void onRunEnd() {
  }

  public MutationStatistics getStatistics() {
    return this.mutatorScores;
  }

}
