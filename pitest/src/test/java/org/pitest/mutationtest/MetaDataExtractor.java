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
package org.pitest.mutationtest;

import java.util.ArrayList;
import java.util.List;

import org.pitest.Description;
import org.pitest.TestResult;
import org.pitest.extension.TestListener;
import org.pitest.functional.Option;
import org.pitest.mutationtest.instrument.MutationMetaData;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.mutationtest.results.MutationResult;

public class MetaDataExtractor implements TestListener {

  private final List<MutationResult> data = new ArrayList<MutationResult>();

  public List<DetectionStatus> getDetectionStatus() {
    final List<DetectionStatus> dss = new ArrayList<DetectionStatus>();
    for (final MutationResult each : this.data) {
      dss.add(each.getStatus());
    }
    return dss;
  }

  private void accumulateMetaData(final TestResult tr) {
    final Option<MutationMetaData> d = tr.getValue(MutationMetaData.class);
    if (d.hasSome()) {
      this.data.addAll(d.value().getMutations());
    }
  }

  public void onTestError(final TestResult tr) {
    accumulateMetaData(tr);
  }

  public void onTestFailure(final TestResult tr) {
    accumulateMetaData(tr);
  }

  public void onTestSkipped(final TestResult tr) {
    accumulateMetaData(tr);
  }

  public void onTestStart(final Description d) {

  }

  public void onTestSuccess(final TestResult tr) {
    accumulateMetaData(tr);
  }

  public void onRunEnd() {
    // TODO Auto-generated method stub
  }

  public void onRunStart() {
    // TODO Auto-generated method stub
  }

  public List<Integer> getLineNumbers() {
    final List<Integer> dss = new ArrayList<Integer>();
    for (final MutationResult each : this.data) {
      dss.add(each.getDetails().getLineNumber());
    }
    return dss;
  }

}