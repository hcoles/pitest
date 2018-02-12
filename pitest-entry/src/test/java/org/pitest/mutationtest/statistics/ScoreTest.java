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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.util.StringUtil;

public class ScoreTest {

  private ScorePrecursor testee;

  @Before
  public void setUp() {
    this.testee = new ScorePrecursor("foo");
  }

  @Test
  public void shouldContainEmptyScoreForEachStatusWhenConstructed() {
    for (final DetectionStatus each : DetectionStatus.values()) {
      assertTrue(FCollection.contains(this.testee.getCounts(),
          countFor(each, 0)));
    }

  }

  @Test
  public void registerResultsShouldIncrementCount() {
    this.testee.registerResult(DetectionStatus.KILLED);
    assertTrue(FCollection.contains(this.testee.getCounts(),
        countFor(DetectionStatus.KILLED, 1)));
  }

  @Test
  public void shouldPrintMutatorNameOnFirstLine() {
    final String[] ss = generateReportLines();
    assertEquals("> foo", ss[0]);
  }

  @Test
  public void shouldPrintTotalsAndPercentageInSecondLine() {
    final String[] ss = generateReportLines();
    assertEquals(">> Generated 0 Killed 0 (100%)", ss[1]);
  }

  @Test
  public void shouldPrintScoresFourToALine() {
    final String[] ss = generateReportLines();
    assertEquals("> KILLED 0 SURVIVED 0 TIMED_OUT 0 NON_VIABLE 0 ", ss[2]);
  }

  private String[] generateReportLines() {
    final ByteArrayOutputStream s = new ByteArrayOutputStream();
    final PrintStream out = new PrintStream(s);
    this.testee.toScore().report(out);
    final String actual = new String(s.toByteArray());
    final String[] ss = actual.split(StringUtil.newLine());
    return ss;
  }

  @Test
  public void shouldCalculateTotalNumberOfMutationsWhenNoneRegistered() {
    assertEquals(0, this.testee.toScore().getTotalMutations());
  }

  @Test
  public void shouldCalculateTotalNumberOfDectedMutationsWhenNoneRegistered() {
    assertEquals(0, this.testee.toScore().getTotalDetectedMutations());
  }

  @Test
  public void shouldCalculateTotalNumberOfMutationsWhenSomeRegistered() {
    this.testee.registerResult(DetectionStatus.KILLED);
    this.testee.registerResult(DetectionStatus.NO_COVERAGE);
    assertEquals(2, this.testee.toScore().getTotalMutations());
  }

  @Test
  public void shouldCalculateTotalNumberOfDetectedMutationsWhenSomeRegistered() {
    this.testee.registerResult(DetectionStatus.KILLED);
    this.testee.registerResult(DetectionStatus.NO_COVERAGE);
    this.testee.registerResult(DetectionStatus.TIMED_OUT);
    this.testee.registerResult(DetectionStatus.SURVIVED);
    assertEquals(2, this.testee.toScore().getTotalDetectedMutations());
  }

  @Test
  public void shouldCalculatePercentageDetectedWhenNoneFound() {
    assertEquals(100, this.testee.toScore().getPercentageDetected());
  }

  @Test
  public void shouldCalculatePercentageDetectedWhenNoneDetected() {
    this.testee.registerResult(DetectionStatus.SURVIVED);
    assertEquals(0, this.testee.toScore().getPercentageDetected());
  }

  @Test
  public void shouldCalculatePercentageDetectedWhenSomeDetected() {
    registerResults(DetectionStatus.SURVIVED, 2);
    registerResults(DetectionStatus.KILLED, 1);
    assertEquals(33, this.testee.toScore().getPercentageDetected());
  }

  @Test
  public void shouldCalculatePercentageDetectedWhenAllDetected() {
    registerResults(DetectionStatus.KILLED, 8);
    assertEquals(100, this.testee.toScore().getPercentageDetected());
  }

  private void registerResults(final DetectionStatus status, final int times) {
    for (int i = 0; i != times; i++) {
      this.testee.registerResult(status);
    }
  }

  private Predicate<StatusCount> countFor(final DetectionStatus each,
      final int count) {
    return a -> a.getStatus().equals(each) && (a.getCount() == count);
  }

}
