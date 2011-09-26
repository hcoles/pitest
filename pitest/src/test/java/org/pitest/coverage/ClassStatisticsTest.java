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
package org.pitest.coverage;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class ClassStatisticsTest {

  private ClassStatistics testee;

  @Before
  public void setUp() {
    this.testee = new ClassStatistics("foo");
  }

  @Test
  public void shouldRecordVisitsToEachLine() {
    addLineHits(Arrays.asList(1, 1, 1, 2, 2, 20));
    final Set<Integer> expected = new HashSet<Integer>(Arrays.asList(1, 2, 20));
    assertEquals(expected, this.testee.getUniqueVisitedLines());
  }

  private void addLineHits(final List<Integer> coveredLines) {
    for (final int each : coveredLines) {
      this.testee.registerLineVisit(each);
    }
  }

}
