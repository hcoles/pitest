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
package org.pitest.extension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.Description;
import org.pitest.DescriptionMother;
import org.pitest.TestResult;

public class CompoundTestListenerTest {

  private CompoundTestListener testee;

  @Mock
  private TestListener         firstChild;

  @Mock
  private TestListener         secondChild;

  @Mock
  private TestResult           result;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new CompoundTestListener(Arrays.asList(this.firstChild,
        this.secondChild));
  }

  @Test
  public void shouldCallOnRunStartForAllChildren() {
    this.testee.onRunStart();
    verify(this.firstChild, times(1)).onRunStart();
    verify(this.secondChild, times(1)).onRunStart();
  }

  @Test
  public void shouldCallOnRunEndForAllChildren() {
    this.testee.onRunEnd();
    verify(this.firstChild, times(1)).onRunEnd();
    verify(this.secondChild, times(1)).onRunEnd();
  }

  @Test
  public void shouldCallOnTestErrorForAllChildren() {
    this.testee.onTestError(this.result);
    verify(this.firstChild, times(1)).onTestError(this.result);
    verify(this.secondChild, times(1)).onTestError(this.result);
  }

  @Test
  public void shouldCallOnTestFailureForAllChildren() {
    this.testee.onTestFailure(this.result);
    verify(this.firstChild, times(1)).onTestFailure(this.result);
    verify(this.secondChild, times(1)).onTestFailure(this.result);
  }

  @Test
  public void shouldCallOnTestSkippedForAllChildren() {
    this.testee.onTestSkipped(this.result);
    verify(this.firstChild, times(1)).onTestSkipped(this.result);
    verify(this.secondChild, times(1)).onTestSkipped(this.result);
  }

  @Test
  public void shouldCallOnTestSuccessForAllChildren() {
    this.testee.onTestSuccess(this.result);
    verify(this.firstChild, times(1)).onTestSuccess(this.result);
    verify(this.secondChild, times(1)).onTestSuccess(this.result);
  }

  @Test
  public void shouldCallOnTestStartForAllChildren() {
    final Description description = DescriptionMother
        .createEmptyDescription("foo");
    this.testee.onTestStart(description);
    verify(this.firstChild, times(1)).onTestStart(description);
    verify(this.secondChild, times(1)).onTestStart(description);
  }

}
