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
package org.pitest;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.extension.TestListener;

public class ResultTypeTest {

  @Mock
  private TestListener listener;

  @Mock
  private TestResult   result;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testPassListenerFunctionCallsListenerSuccess() {
    callListenerFunction(ResultType.PASS);
    verify(this.listener).onTestSuccess(this.result);
  }

  @Test
  public void testFailListenerFunctionCallsListenerFail() {
    callListenerFunction(ResultType.FAIL);
    verify(this.listener).onTestFailure(this.result);
  }

  @Test
  public void testErrorListenerFunctionCallsListenerError() {
    callListenerFunction(ResultType.ERROR);
    verify(this.listener).onTestError(this.result);
  }

  @Test
  public void testSkippedListenerFunctionCallsListenerSkipped() {
    callListenerFunction(ResultType.SKIPPED);
    verify(this.listener).onTestSkipped(this.result);
  }

  @Test
  public void testStartedListenerFunctionCallsListenerStart() {
    callListenerFunction(ResultType.STARTED);
    verify(this.listener).onTestStart(this.result.getTest());
  }

  private void callListenerFunction(final ResultType testee) {
    testee.getListenerFunction(this.result).apply(this.listener);
  }
}
