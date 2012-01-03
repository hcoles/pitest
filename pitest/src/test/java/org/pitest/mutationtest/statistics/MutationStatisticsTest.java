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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.execute.MutationStatusTestPair;
import org.pitest.mutationtest.report.MutationTestResultMother;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.mutationtest.results.MutationResult;

public class MutationStatisticsTest {

  private MutationStatistics testee;

  @Before
  public void setUp() {
    this.testee = new MutationStatistics();
  }

  @Test
  public void shouldContainNoResultsWhenFirstConstructed() {
    assertFalse(this.testee.getScores().iterator().hasNext());
  }

  @Test
  public void shouldRecordsStatisticsAgainstMutators() {
    final MutationResult mr = makeResult();
    this.testee.registerResults(Collections.singletonList(mr));
    assertTrue(FCollection.contains(this.testee.getScores(),
        hasResultForMutator(mr.getDetails().getId().getMutator())));
  }

  private F<Score, Boolean> hasResultForMutator(final String mutator) {
    return new F<Score, Boolean>() {

      public Boolean apply(final Score a) {
        return a.getMutatorName().equals(mutator);
      }
    };
  }

  private MutationResult makeResult() {
    final MutationResult mr = new MutationResult(
        MutationTestResultMother.createDetails("foo.java"),
        new MutationStatusTestPair(DetectionStatus.KILLED, "foo"));
    return mr;
  }

}
