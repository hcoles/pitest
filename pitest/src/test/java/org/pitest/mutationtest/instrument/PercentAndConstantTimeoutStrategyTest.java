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
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.util.SysdateFunction;

public class PercentAndConstantTimeoutStrategyTest {

  private final static long                 CURRENT_TIME = 1000000l;

  private PercentAndConstantTimeoutStrategy testee;

  @Mock
  private SysdateFunction                   constantDate;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    when(this.constantDate.getTimeInMilliseconds()).thenReturn(CURRENT_TIME);
  }

  @Test
  public void shouldReturnProductOfFactorAnTestTimePlusConstant() {
    final long time = 123;
    this.testee = new PercentAndConstantTimeoutStrategy(this.constantDate,
        1.2f, 666);
    assertEquals((Math.round(1.2f * time) + 666) + CURRENT_TIME,
        this.testee.getEndTime(time));

  }
}
