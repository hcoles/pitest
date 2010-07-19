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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.Description;
import org.pitest.TestResult;
import org.pitest.TimeoutException;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;
import org.pitest.testunit.AbstractTestUnit;

public class TimeoutDecoratorTest {

  private Description      description;

  private TimeoutDecorator testee;

  private ClassLoader      loader;

  @Mock
  private ResultCollector  rc;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.loader = Thread.currentThread().getContextClassLoader();
    this.description = new Description("foo", TimeoutDecoratorTest.class, null);
  }

  @Test
  public void testReportsSuccessIfTestCompletesBeforeTimeOut() {
    final TestUnit quickComplete = new AbstractTestUnit(this.description) {
      @Override
      public void execute(final ClassLoader loader, final ResultCollector rc) {
        rc.notifyEnd(TestResult.Success(TimeoutDecoratorTest.this.description));
      }

    };

    this.testee = new TimeoutDecorator(quickComplete, 100);
    this.testee.execute(this.loader, this.rc);
    verify(this.rc).notifyEnd(eq(TestResult.Success(this.description)));
  }

  @Test
  public void testReportsErrorIfTestCompletesAfterTimeOut() {
    final TestUnit slowComplete = new AbstractTestUnit(this.description) {
      @Override
      public void execute(final ClassLoader loader, final ResultCollector rc) {
        try {
          Thread.sleep(1000);
        } catch (final InterruptedException e) {
          // swallow
        }
        rc.notifyEnd(TestResult.Success(TimeoutDecoratorTest.this.description));
      }

    };

    this.testee = new TimeoutDecorator(slowComplete, 100);
    this.testee.execute(this.loader, this.rc);

    verify(this.rc).notifyEnd(
        eq(new TestResult(slowComplete, new TimeoutException(
            "Test timed out after 100 milliseconds"))));

    verify(this.rc, never())
        .notifyEnd(eq(TestResult.Success(this.description)));

  }

}
