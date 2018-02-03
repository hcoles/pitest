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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestGroupConfig;

import com.example.testng.Fails;
import com.example.testng.HasGroups;
import com.example.testng.Passes;
import com.example.testng.Skips;

import junit.framework.AssertionFailedError;

public class TestNGTestUnitTest {

  @Mock
  private ResultCollector rc;

  private TestNGTestUnit     testee;
  private TestGroupConfig    config;
  private Collection<String> includedTestMethods;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.config = new TestGroupConfig(Collections.<String> emptyList(),
        Collections.<String> emptyList());
    this.includedTestMethods = Collections.emptyList();
  }

  @Test
  public void shouldReportTestClassStart() {
    this.testee = new TestNGTestUnit(Passes.class, this.config, this.includedTestMethods);
    this.testee.execute(this.rc);
    verify(this.rc, times(1)).notifyStart(this.testee.getDescription());
  }


  @Test
  public void shouldReportTestMethodStart() {
    this.testee = new TestNGTestUnit(Passes.class, this.config, this.includedTestMethods);
    this.testee.execute(this.rc);
    verify(this.rc, times(1)).notifyStart(
        new Description("passes", Passes.class));
  }

  @Test
  public void shouldReportTestEndWithoutErorWhenTestRunsSuccessfully() {
    this.testee = new TestNGTestUnit(Passes.class, this.config, this.includedTestMethods);
    this.testee.execute(this.rc);
    verify(this.rc, times(1))
    .notifyEnd(new Description("passes", Passes.class));
  }

  @Test
  public void shouldReportTestEndWithThrowableWhenTestFails() {
    this.testee = new TestNGTestUnit(Fails.class, this.config, this.includedTestMethods);
    this.testee.execute(this.rc);
    verify(this.rc, times(1)).notifyEnd(
        eq(new Description("fails", Fails.class)),
        any(AssertionFailedError.class));
  }

  @Test
  public void shouldSkipPassingTestsAfterAFailure() {
    this.testee = new TestNGTestUnit(Fails.class, this.config, this.includedTestMethods);
    this.testee.execute(this.rc);
    verify(this.rc, times(1)).notifySkipped(
        eq(new Description("passes", Fails.class)));
  }

  // we have static state so history may affect results
  @Test
  public void shouldRunTestsInNextTestClassAferFailure() {
    new TestNGTestUnit(Fails.class, this.config, this.includedTestMethods).execute(Mockito.mock(ResultCollector.class));

    this.testee = new TestNGTestUnit(Passes.class, this.config, this.includedTestMethods);
    this.testee.execute(this.rc);

    verify(this.rc, times(1))
    .notifyEnd(new Description("passes", Passes.class));;
  }

  @Test
  public void shouldNotRunTestsInExcludedGroups() {
    final TestGroupConfig excludeConfig = new TestGroupConfig(
        Arrays.asList("exclude"), Collections.<String> emptyList());
    this.testee = new TestNGTestUnit(HasGroups.class, excludeConfig, this.includedTestMethods);
    this.testee.execute(this.rc);
    verify(this.rc, times(1)).notifyEnd(
        new Description("includeGroup", HasGroups.class));
    verify(this.rc, times(1)).notifyEnd(
        new Description("noGroup", HasGroups.class));
  }

  @Test
  public void shouldOnlyRunTestsInIncludedGroups() {
    final TestGroupConfig excludeConfig = new TestGroupConfig(
        Collections.<String> emptyList(), Arrays.asList("include"));
    this.testee = new TestNGTestUnit(HasGroups.class, excludeConfig, this.includedTestMethods);
    this.testee.execute(this.rc);
    verify(this.rc, times(1)).notifyEnd(
        new Description("includeGroup", HasGroups.class));
    verify(this.rc, times(1)).notifyEnd(
        new Description("includeAndExcludeGroup", HasGroups.class));
  }

  @Test
  public void shouldOnlyRunTestsInIncludedTestMethods() {
    final List<String> includedMethods = new ArrayList<>();
    includedMethods.add("includeGroup");
    includedMethods.add("excludeGroup");

    this.testee = new TestNGTestUnit(HasGroups.class, this.config, includedMethods);
    this.testee.execute(this.rc);
    verify(this.rc, times(1)).notifyEnd(
            new Description("includeGroup", HasGroups.class));
    verify(this.rc, times(1)).notifyEnd(
            new Description("excludeGroup", HasGroups.class));
  }

  @Test
  public void shouldReportTestSkipped() {
    this.testee = new TestNGTestUnit(Skips.class, this.config, this.includedTestMethods);
    this.testee.execute(this.rc);
    verify(this.rc, times(1)).notifySkipped(
        eq(new Description("skip", Skips.class)));
  }

}
