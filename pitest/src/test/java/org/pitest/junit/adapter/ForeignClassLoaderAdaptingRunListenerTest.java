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

package org.pitest.junit.adapter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.DescriptionMother;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.foreignclassloader.Events;

public class ForeignClassLoaderAdaptingRunListenerTest {

  private ForeignClassLoaderAdaptingRunListener testee;

  private List<String>                          queue;

  @Mock
  private Throwable                             throwable;

  private org.pitest.testapi.Description        pitDescription;

  @Mock
  private Description                           junitDesc;

  @Mock
  private ResultCollector                       rc;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.queue = new ArrayList<String>();
    this.pitDescription = DescriptionMother.createEmptyDescription("foo");
    this.testee = new ForeignClassLoaderAdaptingRunListener(this.queue);
  }

  @Test
  public void shouldReportExceptionOnFailure() throws Exception {
    this.testee.testFailure(new Failure(this.junitDesc, this.throwable));
    applyEvents();
    verify(this.rc).notifyEnd(eq(this.pitDescription), any(Throwable.class));
  }

  @Test
  public void shouldNotReportTestEndWithoutErrorAfterFailure() throws Exception {
    this.testee.testFailure(new Failure(this.junitDesc, this.throwable));
    this.testee.testFinished(this.junitDesc);
    applyEvents();
    verify(this.rc, never()).notifyEnd(this.pitDescription);
  }

  @Test
  public void shouldTreatAssumptionFailureAsSuccess() throws Exception {
    this.testee.testAssumptionFailure(new Failure(this.junitDesc,
        this.throwable));
    this.testee.testFinished(this.junitDesc);
    applyEvents();
    verify(this.rc, never()).notifyEnd(this.pitDescription, this.throwable);
    verify(this.rc).notifyEnd(this.pitDescription);
  }

  @Test
  public void shouldReportIgnoredTestsAsSkipped() throws Exception {
    this.testee.testIgnored(this.junitDesc);
    applyEvents();
    verify(this.rc).notifySkipped(this.pitDescription);
  }

  @Test
  public void shouldNotReportTestEndWithoutErrorAfterIgnore() throws Exception {
    this.testee.testIgnored(this.junitDesc);
    this.testee.testFinished(this.junitDesc);
    applyEvents();
    verify(this.rc, never()).notifyEnd(this.pitDescription);
  }

  @Test
  public void shouldReportStartedTests() throws Exception {
    this.testee.testStarted(this.junitDesc);
    applyEvents();
    verify(this.rc).notifyStart(this.pitDescription);
  }

  private void applyEvents() {
    Events.applyEvents(this.queue, this.rc, this.pitDescription);
  }
}
