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
package org.pitest.extension.common;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.MultipleTestGroup;
import org.pitest.extension.TestUnit;

public class GroupPerClassStrategyTest {

  @Mock
  TestUnit tu1;

  @Mock
  TestUnit tu2;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testReturnsSingleGroupContainingAllTests() {
    final GroupPerClassStrategy testee = new GroupPerClassStrategy();

    final Collection<TestUnit> tus = Arrays.asList(this.tu1, this.tu2);
    final Collection<? extends TestUnit> actual = testee.group(null, tus);
    final Collection<? extends TestUnit> expected = Collections
        .singleton(new MultipleTestGroup(tus));
    assertEquals(expected, actual);
  }

}
