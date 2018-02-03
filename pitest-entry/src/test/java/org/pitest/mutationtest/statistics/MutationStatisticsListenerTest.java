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

import static org.junit.Assert.assertTrue;

import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.report.MutationTestResultMother;

public class MutationStatisticsListenerTest {

  private MutationStatisticsListener testee;

  @Before
  public void setUp() {
    this.testee = new MutationStatisticsListener();
  }

  @Test
  public void shouldGatherStatistics() {
    final MutationResult mr = makeResult();
    this.testee.handleMutationResult(createMetaData(mr));
    assertTrue(hasResultFor(mr.getDetails().getId().getMutator()));
  }

  private MutationResult makeResult() {
    final MutationResult mr = new MutationResult(
        MutationTestResultMother.createDetails("foo.java"),
        new MutationStatusTestPair(1, DetectionStatus.KILLED, "foo"));
    return mr;
  }

  private boolean hasResultFor(final String mutator) {
    return FCollection.contains(this.testee.getStatistics().getScores(),
        hasResultForMutator(mutator));
  }

  private Predicate<Score> hasResultForMutator(final String mutator) {
    return a -> a.getMutatorName().equals(mutator);
  }

  private ClassMutationResults createMetaData(final MutationResult... mrs) {
    return MutationTestResultMother.createClassResults(mrs);
  }

}
