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
package org.pitest.mutationtest.instrument;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.extension.TestUnit;

public class StatisticsTest {

  private Statistics testee;

  @Mock
  private TestUnit   t1;

  @Mock
  private TestUnit   t2;

  @Mock
  private TestUnit   t3;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    final Map<ClassLine, List<TestUnit>> stats = new HashMap<ClassLine, List<TestUnit>>();
    stats
        .put(new ClassLine("foo", 1), Arrays.asList(this.t1, this.t3, this.t2));
    stats
        .put(new ClassLine("foo", 2), Arrays.asList(this.t3, this.t2, this.t1));

    final Map<TestUnit, Long> times = new HashMap<TestUnit, Long>();
    times.put(this.t1, 1l);
    times.put(this.t2, 2l);
    times.put(this.t3, 3l);
    this.testee = new Statistics(true, times, stats);
  };

  @Test
  public void testListsOfTestsOrderByIncreasingTime() {

    final List<TestUnit> actualForLine1 = this.testee
        .getTestForLineNumber(new ClassLine("foo", 1));
    final List<TestUnit> actualForLine2 = this.testee
        .getTestForLineNumber(new ClassLine("foo", 2));

    final List<TestUnit> expected = Arrays.asList(this.t1, this.t2, this.t3);
    assertEquals(expected, actualForLine1);
    assertEquals(expected, actualForLine2);

  }

  @Test
  public void testGetExecutionTimeReturnsExecutionTimes() {
    assertEquals(3, this.testee.getExecutionTime(this.t3));
    assertEquals(2, this.testee.getExecutionTime(this.t2));

  }

  @Test
  public void testReturnsEmptyListIfNoTestsForLineNumber() {
    assertEquals(Collections.emptyList(),
        this.testee.getTestForLineNumber(new ClassLine("foo", 100)));
  }

}
