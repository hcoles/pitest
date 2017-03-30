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

import java.util.Arrays;
import java.util.Collections;

import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.ClassLoaderDetectionStrategy;
import org.pitest.util.IsolationUtils;

import com.example.testng.Fails;
import com.example.testng.HasGroups;
import com.example.testng.Passes;
import com.example.testng.Skips;

public class TestNGTestUnitTest {

  @Mock
  private ResultCollector rc;

  private ClassLoader     loader;
  private TestNGTestUnit  testee;
  private TestGroupConfig config;

  @Before
  public void setUp() {
    this.loader = IsolationUtils.getContextClassLoader();
    MockitoAnnotations.initMocks(this);
    this.config = new TestGroupConfig(Collections.<String> emptyList(),
        Collections.<String> emptyList());
  }

  @Test
  public void shouldReportTestClassStart() {
    this.testee = new TestNGTestUnit(Passes.class, this.config);
    this.testee.execute(this.loader, this.rc);
    verify(this.rc, times(1)).notifyStart(this.testee.getDescription());
  }

  @Test
  public void shouldReportTestClassStartWhenExecutingInForeignClassLoader() {
    this.testee = new TestNGTestUnit(neverMatch(), Passes.class, this.config);
    this.testee.execute(this.loader, this.rc);
    verify(this.rc, times(1)).notifyStart(this.testee.getDescription());
  }

  @Test
  public void shouldReportTestMethodStart() {
    this.testee = new TestNGTestUnit(Passes.class, this.config);
    this.testee.execute(this.loader, this.rc);
    verify(this.rc, times(1)).notifyStart(
        new Description("passes", Passes.class));
  }

  @Test
  public void shouldReportTestMethodStartWhenExecutingInForeignClassLoader() {
    this.testee = new TestNGTestUnit(neverMatch(), Passes.class, this.config);
    this.testee.execute(this.loader, this.rc);
    verify(this.rc, times(1)).notifyStart(
        new Description("passes", Passes.class));
  }

  @Test
  public void shouldReportTestEndWithoutErorWhenTestRunsSuccessfully() {
    this.testee = new TestNGTestUnit(Passes.class, this.config);
    this.testee.execute(this.loader, this.rc);
    verify(this.rc, times(1))
    .notifyEnd(new Description("passes", Passes.class));
  }

  @Test
  public void shouldReportTestEndWithoutErorWhenTestRunsSuccessfullyInForeignClassLoader() {
    this.testee = new TestNGTestUnit(neverMatch(), Passes.class, this.config);
    this.testee.execute(this.loader, this.rc);
    verify(this.rc, times(1))
    .notifyEnd(new Description("passes", Passes.class));
  }

  @Test
  public void shouldReportTestEndWithThrowableWhenTestFails() {
    this.testee = new TestNGTestUnit(Fails.class, this.config);
    this.testee.execute(this.loader, this.rc);
    verify(this.rc, times(1)).notifyEnd(
        eq(new Description("fails", Fails.class)),
        any(AssertionFailedError.class));
  }

  @Test
  public void shouldSkipPassingTestsAfterAFailure() {
    this.testee = new TestNGTestUnit(Fails.class, this.config);
    this.testee.execute(this.loader, this.rc);
    verify(this.rc, times(1)).notifySkipped(
        eq(new Description("passes", Fails.class)));
  }
  
  // we have static state so history may affect results
  @Test
  public void shouldRunTestsInNextTestClassAferFailure() {
    new TestNGTestUnit(Fails.class, this.config).execute(loader, Mockito.mock(ResultCollector.class));
    
    this.testee = new TestNGTestUnit(neverMatch(), Passes.class, this.config);
    this.testee.execute(this.loader, this.rc);
    
    verify(this.rc, times(1))
    .notifyEnd(new Description("passes", Passes.class));;
  }


  @Test
  public void shouldReportTestEndWithThrowableWhenTestFailsInForeignClassLoader() {
    this.testee = new TestNGTestUnit(neverMatch(), Fails.class, this.config);
    this.testee.execute(this.loader, this.rc);
    verify(this.rc, times(1)).notifyEnd(
        eq(new Description("fails", Fails.class)),
        any(AssertionFailedError.class));
  }

  @Test
  public void shouldSkipPassingTestsAfterAFailureInForeignClassLoader() {
    this.testee = new TestNGTestUnit(neverMatch(), Fails.class, this.config);
    this.testee.execute(this.loader, this.rc);
    verify(this.rc, times(1)).notifySkipped(
        eq(new Description("passes", Fails.class)));
  }

  @Test
  public void shouldNotRunTestsInExcludedGroups() {
    final TestGroupConfig excludeConfig = new TestGroupConfig(
        Arrays.asList("exclude"), Collections.<String> emptyList());
    this.testee = new TestNGTestUnit(HasGroups.class, excludeConfig);
    this.testee.execute(this.loader, this.rc);
    verify(this.rc, times(1)).notifyEnd(
        new Description("includeGroup", HasGroups.class));
    verify(this.rc, times(1)).notifyEnd(
        new Description("noGroup", HasGroups.class));
  }

  @Test
  public void shouldOnlyRunTestsInIncludedGroups() {
    final TestGroupConfig excludeConfig = new TestGroupConfig(
        Collections.<String> emptyList(), Arrays.asList("include"));
    this.testee = new TestNGTestUnit(HasGroups.class, excludeConfig);
    this.testee.execute(this.loader, this.rc);
    verify(this.rc, times(1)).notifyEnd(
        new Description("includeGroup", HasGroups.class));
    verify(this.rc, times(1)).notifyEnd(
        new Description("includeAndExcludeGroup", HasGroups.class));
  }

  @Test
  public void shouldReportTestSkipped() {
    this.testee = new TestNGTestUnit(Skips.class, this.config);
    this.testee.execute(this.loader, this.rc);
    verify(this.rc, times(1)).notifySkipped(
        eq(new Description("skip", Skips.class)));
  }

  @Test
  public void shouldReportTestSkippedInForeignClassloader() {
    this.testee = new TestNGTestUnit(neverMatch(), Skips.class, this.config);
    this.testee.execute(this.loader, this.rc);
    verify(this.rc, times(1)).notifySkipped(
        eq(new Description("skip", Skips.class)));
  }

  private ClassLoaderDetectionStrategy neverMatch() {
    return new ClassLoaderDetectionStrategy() {
      @Override
      public boolean fromDifferentLoader(final Class<?> clazz,
          final ClassLoader loader) {
        return true;
      }

    };
  }

}
