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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;

public class TimingMetaDataResultCollectorTest {

  @Mock
  private ResultCollector               rc;

  private Description                   description;

  private TimingMetaDataResultCollector testee;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new TimingMetaDataResultCollector(this.rc, 42);
    this.description = new Description("foo",
        TimingMetaDataResultCollectorTest.class);
  }

  @Test
  public void shouldNotifyChildOfTestStart() {
    this.testee.notifyStart(this.description);
    verify(this.rc, times(1)).notifyStart(this.description);
  }

  @Test
  public void shouldNotifyChildOfTestSkipped() {
    this.testee.notifySkipped(this.description);
    verify(this.rc, times(1)).notifySkipped(this.description);
  }

  @Test
  public void shouldNotifyChildOfTestSuccessWithTimingMetaData() {
    this.testee.notifyEnd(this.description);
    verify(this.rc, times(1)).notifyEnd(eq(this.description),
        eq(new TimingMetaData(42)));
  }

  @Test
  public void shouldNotifyChildOfTestFailureWithTimingMetaData() {
    final RuntimeException er = new RuntimeException();
    this.testee.notifyEnd(this.description, er);
    verify(this.rc, times(1)).notifyEnd(eq(this.description), eq(er),
        eq(new TimingMetaData(42)));
  }

  @Test
  public void shouldRequestStopWhenChildRequestsStop() {
    when(this.rc.shouldExit()).thenReturn(true);
    assertTrue(this.testee.shouldExit());
  }

  @Test
  public void shouldNotRequestStopWhenChildDoesNotRequestsStop() {
    when(this.rc.shouldExit()).thenReturn(false);
    assertFalse(this.testee.shouldExit());
  }
}
