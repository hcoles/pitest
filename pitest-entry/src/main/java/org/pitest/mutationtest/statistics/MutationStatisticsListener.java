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

import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.MutationResultListener;

public class MutationStatisticsListener implements MutationResultListener,
MutationStatisticsSource {

  private final MutationStatisticsPrecursor mutatorScores = new MutationStatisticsPrecursor();

  @Override
  public MutationStatistics getStatistics() {
    return this.mutatorScores.toStatistics();
  }

  @Override
  public void runStart() {

  }

  @Override
  public void handleMutationResult(final ClassMutationResults metaData) {
    processMetaData(metaData);
  }

  @Override
  public void runEnd() {

  }

  private void processMetaData(final ClassMutationResults value) {
    this.mutatorScores.registerResults(value.getMutations());
  }
}
