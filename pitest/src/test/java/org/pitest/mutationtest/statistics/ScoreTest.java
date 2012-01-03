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

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.util.StringUtil;

public class ScoreTest {

  private Score testee;

  @Before
  public void setUp() {
    this.testee = new Score("foo");
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
  public void shouldPrintScoresFourToALine() {
    final ByteArrayOutputStream s = new ByteArrayOutputStream();
    final PrintStream out = new PrintStream(s);
    this.testee.report(out);
    final String actual = new String(s.toByteArray());
    final String[] ss = actual.split(StringUtil.newLine());
    assertEquals("> foo", ss[0]);
    assertEquals("> KILLED 0 SURVIVED 0 TIMED_OUT 0 NON_VIABLE 0 ", ss[1]);
  }

  private F<StatusCount, Boolean> countFor(final DetectionStatus each,
      final int count) {
    return new F<StatusCount, Boolean>() {

      public Boolean apply(final StatusCount a) {
        return a.status.equals(each) && (a.count == count);
      }

    };
  }

}
