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
package org.pitest.testng;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

public class TestNGAdapterTest {

  private TestNGAdapter   testee;

  @Mock
  private ResultCollector rc;

  @Mock
  private ITestResult     result;

  @Mock
  private ITestNGMethod   method;

  private Description     description;
  private Class<?>        clazz;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.description = new Description("foo");
    this.clazz = TestNGAdapterTest.class;
    this.testee = new TestNGAdapter(this.clazz, this.description, this.rc);
    when(this.method.getMethodName()).thenReturn("method");
    when(this.result.getMethod()).thenReturn(this.method);
  }

  @Test
  public void shouldCallNotifyStartWhenTestStarts() {
    this.testee.onStart(null);
    verify(this.rc, times(1)).notifyStart(this.description);
  }

  @Test
  public void shouldCallNotifyEndWhenTestEndWithoutError() {
    this.testee.onFinish(null);
    verify(this.rc, times(1)).notifyEnd(this.description);
  }

  @Test
  public void shouldCallNotifyEndWithErrorWhenTestEndsFollowingError() {
    final Throwable throwable = new RuntimeException();
    when(this.result.getThrowable()).thenReturn(throwable);
    this.testee.onTestFailure(this.result);
    this.testee.onFinish(null);
    verify(this.rc, times(1)).notifyEnd(this.description, throwable);
  }

  @Test
  public void shouldCallStartAndEndForMethodWhenNoError() {
    this.testee.onTestStart(this.result);
    this.testee.onTestSuccess(this.result);
    verify(this.rc, times(1))
    .notifyStart(new Description("method", this.clazz));
    verify(this.rc, times(1)).notifyEnd(new Description("method", this.clazz));
  }

  @Test
  public void shouldCallStartAndEndForMethodWhenError() {
    final Throwable throwable = new RuntimeException();
    when(this.result.getThrowable()).thenReturn(throwable);
    this.testee.onTestStart(this.result);
    this.testee.onTestFailure(this.result);
    verify(this.rc, times(1))
    .notifyStart(new Description("method", this.clazz));
    verify(this.rc, times(1)).notifyEnd(new Description("method", this.clazz),
        throwable);
  }
}
