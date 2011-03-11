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
package org.pitest.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.ConcreteConfiguration;
import org.pitest.extension.TestDiscoveryListener;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.IdentityTestUnitProcessor;
import org.pitest.internal.TestClass;

import com.example.TheoryTest;

public class JUnitCustomRunnerTestUnitFinderTest {

  private JUnitCustomRunnerTestUnitFinder testee;

  @Mock
  private TestDiscoveryListener           listener;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    this.testee = new JUnitCustomRunnerTestUnitFinder();
  }

  @Test
  public void shouldNotHandlePreviouslyHandledClass() {
    assertFalse(this.testee.canHandle(null, true));
  }

  @Test
  public void shouldHandleUnHandledClass() {
    assertTrue(this.testee.canHandle(null, false));
  }

  @Test
  public void shouldFindTestsInJUnitTheoryTest() {
    final Collection<TestUnit> actual = findWithTestee(TheoryTest.class);
    assertEquals(3, actual.size());
  }

  public static class CustomSuiteRunner extends Suite {

    public CustomSuiteRunner(final Class<?> klass, final RunnerBuilder rb)
        throws InitializationError {
      super(klass, rb);
    }

  }

  public static class One {
    @Test
    public void one() {

    }

    @Test
    public void two() {

    }
  }

  public static class Two {
    @Test
    public void one() {

    }

    @Test
    public void two() {

    }
  }

  @RunWith(CustomSuiteRunner.class)
  @SuiteClasses({ One.class, Two.class })
  public static class CustomSuite {

  }

  @Test
  public void shouldNotFindTestsInCustomSuite() {
    final Collection<TestUnit> actual = findWithTestee(CustomSuite.class);
    assertTrue(actual.isEmpty());
  }

  public static class Three {
    @Test
    public void one() {

    }

    @Test
    public void two() {

    }
  }

  @RunWith(CustomSuiteRunner.class)
  @SuiteClasses({ CustomSuite.class, Three.class })
  public static class NestCustomSuite {

  }

  @Test
  public void shouldNotFindTestsInNestedCustomSuites() {
    final Collection<TestUnit> actual = findWithTestee(NestCustomSuite.class);
    assertTrue(actual.isEmpty());
  }

  private Collection<TestUnit> findWithTestee(final Class<?> clazz) {
    return this.testee.findTestUnits(new TestClass(clazz),
        new ConcreteConfiguration(false), this.listener,
        new IdentityTestUnitProcessor());
  }

}
