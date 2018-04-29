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
package org.pitest.testapi.execute;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;

public class ExitingResultCollectorTest {

  @Mock
  ResultCollector        rc;

  Description            description;

  ExitingResultCollector testee;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new ExitingResultCollector(this.rc);
    this.description = new Description("foo", ExitingResultCollectorTest.class);
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
  public void shouldNotifyChildOfTestSuccess() {
    this.testee.notifyEnd(this.description);
    verify(this.rc, times(1)).notifyEnd(this.description);
  }

  @Test
  public void shouldNotifyChildOfTestFailure() {
    final RuntimeException er = new RuntimeException();
    this.testee.notifyEnd(this.description, er);
    verify(this.rc, times(1)).notifyEnd(this.description, er);
  }

  @Test
  public void shouldRequestStopOnTestFailure() {
    final RuntimeException er = new RuntimeException();
    assertFalse(this.testee.shouldExit());
    this.testee.notifyEnd(this.description, er);
    assertTrue(this.testee.shouldExit());
  }

}
