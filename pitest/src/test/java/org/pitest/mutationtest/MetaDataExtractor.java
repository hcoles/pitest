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

import org.pitest.functional.F2;
import org.pitest.functional.FCollection;

public class MetaDataExtractor implements MutationResultListener {

  private final List<MutationResult> data = new ArrayList<MutationResult>();

  public List<DetectionStatus> getDetectionStatus() {
    final List<DetectionStatus> dss = new ArrayList<DetectionStatus>();
    for (final MutationResult each : this.data) {
      dss.add(each.getStatus());
    }
    return dss;
  }

  public List<Integer> getLineNumbers() {
    final List<Integer> dss = new ArrayList<Integer>();
    for (final MutationResult each : this.data) {
      dss.add(each.getDetails().getLineNumber());
    }
    return dss;
  }

  public int getNumberOfTestsRun() {
    final F2<Integer, MutationResult, Integer> sum = new F2<Integer, MutationResult, Integer>() {
      @Override
      public Integer apply(final Integer a, final MutationResult b) {
        return a + b.getNumberOfTestsRun();
      }

    };
    return FCollection.fold(sum, 0, this.data);
  }

  @Override
  public void runStart() {

  }

  @Override
  public void handleMutationResult(final ClassMutationResults metaData) {
    this.data.addAll(metaData.getMutations());
  }

  @Override
  public void runEnd() {
    // TODO Auto-generated method stub

  }

}