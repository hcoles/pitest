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
package org.pitest.mutationtest.execute;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.functional.SideEffect;
import org.pitest.mutationtest.TimeoutLengthStrategy;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestUnit;

public class MutationTimeoutDecoratorTest {

  private MutationTimeoutDecorator testee;

  @Mock
  private SideEffect               sideEffect;

  @Mock
  private TimeoutLengthStrategy    timeoutStrategy;

  @Mock
  private TestUnit                 child;

  @Mock
  private ResultCollector          rc;

  private static final long        NORMAL_EXECUTION = 1;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new MutationTimeoutDecorator(this.child, this.sideEffect,
        this.timeoutStrategy, NORMAL_EXECUTION);
  }

  @Test
  public void shouldCompleteNormallyWhenChildExecutesWithinAllowedTime() {
    when(this.timeoutStrategy.getAllowedTime(NORMAL_EXECUTION)).thenReturn(
        1000l);
    this.testee.execute(this.rc);
    verify(this.child).execute(any(ResultCollector.class));
    verify(this.sideEffect, never()).apply();
  }

  @Test
  @Ignore("flakey")
  public void shouldApplySideEffectWhenChildRunsForLongerThanAllowedTime() {
    when(this.timeoutStrategy.getAllowedTime(NORMAL_EXECUTION)).thenReturn(50l);

    doAnswer(invocation -> {
      Thread.sleep(100);
      return null;
    }).when(this.child).execute(any(ResultCollector.class));

    this.testee.execute(this.rc);
    verify(this.child).execute(any(ResultCollector.class));
    verify(this.sideEffect).apply();
  }
}
