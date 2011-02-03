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
package org.pitest.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class RangeTest {

  @Test
  public void shouldIterateOverAllNumbersInRange() {
    final Range testee = new Range(2, 4);
    final List<Integer> actual = new ArrayList<Integer>();
    for (final int each : testee) {
      actual.add(each);
    }
    final List<Integer> expected = new ArrayList<Integer>();
    expected.add(2);
    expected.add(3);
    expected.add(4);
    assertEquals(expected, actual);
  }

  @Test
  public void testgetLastNumberInRangeReturnsLastNumber() {
    final Range testee = new Range(1, 100000);
    assertEquals(100000, testee.getLastNumberInRange());

  }

}
