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
package org.pitest.mutationtest;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.IdentityTestUnitProcessor;
import org.pitest.extension.common.NullDiscoveryListener;
import org.pitest.functional.predicate.False;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.instrument.MutationTestUnit;
import org.pitest.mutationtest.report.MutationTestSummaryData.MutationTestType;

public class MutationTestFinderTest {

  private MutationTestFinder testee;
  private MutationConfig     config;
  private Configuration      pitConfig;

  @Before
  public void setUp() {
    this.config = DefaultMutationConfigFactory.createConfig(100,
        Mutator.VOID_METHOD_CALLS);
    this.testee = new MutationTestFinder(this.config);
    this.pitConfig = new JUnitCompatibleConfiguration();
  }

  public static class One {
    public void increment() {
      int i = 0;
      i++;
    }
  }

  @MutationTest(threshold = 66, mutators = Mutator.INCREMENTS)
  public static class TestOne {
    @Test
    public void test() {

    }
  }

  @Test
  public void shouldUseConfigInAnnotatedChildInPreferenceToParentSuiteConfig() {
    final Collection<TestUnit> tus = this.testee.findTestUnits(TestOne.class,
        this.pitConfig, new NullDiscoveryListener(),
        new IdentityTestUnitProcessor());
    assertEquals(1, tus.size());
    final MutationTestUnit actual = (MutationTestUnit) tus.iterator().next();
    final MutationEngine engine = DefaultMutationConfigFactory.createEngine(
        true, False.instance(String.class),
        DefaultMutationConfigFactory.LOGGING_CLASSES, Mutator.INCREMENTS);
    assertEquals(new MutationConfig(engine, MutationTestType.TEST_CENTRIC, 66,
        Collections.<String> emptyList()), actual.getMutationConfig());
  }

}
