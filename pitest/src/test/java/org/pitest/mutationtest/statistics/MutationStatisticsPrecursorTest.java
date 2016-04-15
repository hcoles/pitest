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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.report.MutationTestResultMother;
import org.pitest.util.StringUtil;

public class MutationStatisticsPrecursorTest {

  private MutationStatisticsPrecursor testee;

  @Before
  public void setUp() {
    this.testee = new MutationStatisticsPrecursor();
  }

  @Test
  public void shouldContainNoResultsWhenFirstConstructed() {
    assertFalse(this.testee.getScores().iterator().hasNext());
  }

  @Test
  public void shouldRecordStatisticsAgainstMutators() {
    final MutationResult mr = makeResult(DetectionStatus.KILLED);
    this.testee.registerResults(Collections.singletonList(mr));
    assertTrue(FCollection.contains(this.testee.getScores(),
        hasResultForMutator(mr.getDetails().getId().getMutator())));
  }

  @Test
  public void shouldCalculateTotalNumberOfMutationsWhenNoneGenerated() {
    assertEquals(0, this.testee.toStatistics().getTotalMutations());
  }

  @Test
  public void shouldCalculateTotalNumberOfMutationsWhenSomeGenerated() {
    final MutationResult mr = makeResult(DetectionStatus.KILLED);
    this.testee.registerResults(Arrays.asList(mr, mr, mr));
    assertEquals(3, this.testee.toStatistics().getTotalMutations());
  }

  @Test
  public void shouldCalculateTotalNumberOfDetectedMutationsWhenNoneGenerated() {
    assertEquals(0, this.testee.toStatistics().getTotalDetectedMutations());
  }

  @Test
  public void shouldCalculateTotalNumberOfDetectedMutationsWhenNoneDetected() {
    final MutationResult mr = makeResult(DetectionStatus.SURVIVED);
    this.testee.registerResults(Arrays.asList(mr, mr, mr));
    assertEquals(0, this.testee.toStatistics().getTotalDetectedMutations());
  }

  @Test
  public void shouldCalculateTotalNumberOfDetectedMutationsWhenSomeDetected() {
    this.testee.registerResults(Arrays.asList(
        makeResult(DetectionStatus.SURVIVED),
        makeResult(DetectionStatus.KILLED)));
    assertEquals(1, this.testee.toStatistics().getTotalDetectedMutations());
  }

  @Test
  public void shouldCalculatePercentageDetected() {
    this.testee.registerResults(Arrays.asList(
        makeResult(DetectionStatus.SURVIVED),
        makeResult(DetectionStatus.KILLED)));
    assertEquals(50, this.testee.toStatistics().getPercentageDetected());
  }
  
  @Test
  public void shouldReportNumberOfSurvivingMutants() {
    this.testee.registerResults(Arrays.asList(
        makeResult(DetectionStatus.SURVIVED),
        makeResult(DetectionStatus.SURVIVED)));
    assertEquals(2, this.testee.toStatistics().getTotalSurvivingMutations());
  }
  
  @Test
  public void shouldReportNumberOfSurvivingMutantsWhenNoneSurvive() {
    this.testee.registerResults(Arrays.asList(
        makeResult(DetectionStatus.KILLED),
        makeResult(DetectionStatus.TIMED_OUT)));
    assertEquals(0, this.testee.toStatistics().getTotalSurvivingMutations());
  }

  @Test
  public void shouldReportTotalNumberOfMutationsWhenNoneGenerated() {
    this.testee.registerResults(Arrays.asList(
        makeResult(DetectionStatus.SURVIVED),
        makeResult(DetectionStatus.KILLED)));
    final String[] actual = generateReportLines();
    assertEquals(">> Generated 2 mutations Killed 1 (50%)", actual[0]);
  }

  @Test
  public void shouldReportTotalNumberOfTestsRun() {
    this.testee.registerResults(Arrays.asList(
        makeResult(DetectionStatus.SURVIVED, 1),
        makeResult(DetectionStatus.KILLED, 42)));
    final String[] actual = generateReportLines();
    assertEquals(">> Ran 43 tests (21.5 tests per mutation)", actual[1]);
  }

  private F<Score, Boolean> hasResultForMutator(final String mutator) {
    return new F<Score, Boolean>() {

      @Override
      public Boolean apply(final Score a) {
        return a.getMutatorName().equals(mutator);
      }
    };
  }

  private MutationResult makeResult(final DetectionStatus status) {
    return makeResult(status, 0);
  }

  private MutationResult makeResult(final DetectionStatus status,
      final int numberOfTests) {
    final MutationResult mr = new MutationResult(
        MutationTestResultMother.createDetails("foo.java"),
        new MutationStatusTestPair(numberOfTests, status, "foo"));
    return mr;
  }

  private String[] generateReportLines() {
    final ByteArrayOutputStream s = new ByteArrayOutputStream();
    final PrintStream out = new PrintStream(s);
    this.testee.toStatistics().report(out);
    final String actual = new String(s.toByteArray());
    final String[] ss = actual.split(StringUtil.newLine());
    return ss;
  }
}
