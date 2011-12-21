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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.PitError;
import org.pitest.extension.ResultCollector;
import org.pitest.internal.ClassLoaderDetectionStrategy;
import org.pitest.internal.IsolationUtils;

import com.example.testng.Fails;

public class TestNGTestUnitTest {

  @Mock
  private ResultCollector rc;

  private ClassLoader     loader;
  private TestNGTestUnit  testee;

  @Before
  public void setUp() {
    this.loader = IsolationUtils.getContextClassLoader();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldReportTestStart() {
    this.testee = new TestNGTestUnit(Passes.class, "passes");
    this.testee.execute(this.loader, this.rc);
    verify(this.rc, times(1)).notifyStart(this.testee.getDescription());
  }

  @Test
  public void shouldReportTestEndWithoutErorWhenTestRunsSuccessfully() {
    this.testee = new TestNGTestUnit(Passes.class, "passes");
    this.testee.execute(this.loader, this.rc);
    verify(this.rc, times(1)).notifyEnd(this.testee.getDescription());
  }

  @Test
  public void shouldReportTestEndWithThrowableWhenTestFails() {
    this.testee = new TestNGTestUnit(Fails.class, "fails");
    this.testee.execute(this.loader, this.rc);
    verify(this.rc, times(1)).notifyEnd(eq(this.testee.getDescription()),
        any(AssertionFailedError.class));
  }

  @Test
  public void shouldRunOnlyTheRequestedMethod() {
    this.testee = new TestNGTestUnit(HasOnePassingAndOneFailingMethod.class,
        "passes");
    this.testee.execute(this.loader, this.rc);
    verify(this.rc, times(1)).notifyEnd(eq(this.testee.getDescription()));
  }

  @Test(expected = PitError.class)
  public void shouldReportErrorWhenRunInForeignClassLoader() {
    this.testee = new TestNGTestUnit(neverMatch(), Fails.class, null);
    this.testee.execute(this.loader, this.rc);
  }

  private ClassLoaderDetectionStrategy neverMatch() {
    return new ClassLoaderDetectionStrategy() {
      public boolean fromDifferentLoader(Class<?> clazz, ClassLoader loader) {
        return true;
      }

    };
  }

}
