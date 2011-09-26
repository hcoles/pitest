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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collection;

import junit.framework.TestCase;

import org.jmock.MockObjectTestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.extension.TestDiscoveryListener;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitProcessor;
import org.pitest.extension.common.IdentityTestUnitProcessor;
import org.pitest.extension.common.NullDiscoveryListener;

public class CustomJUnit3TestUnitFinderTest {

  private CustomJUnit3TestUnitFinder testee;

  @Mock
  private TestUnitProcessor          processor;

  @Mock
  private TestDiscoveryListener      discoveryListener;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    this.testee = new CustomJUnit3TestUnitFinder();
  }

  public static class JMockTest extends MockObjectTestCase {
    public void testExample() {

    }
  }

  @Test
  public void shouldFindTestUnitsInCustomJUnit3Class() {
    final Collection<TestUnit> actual = this.testee.findTestUnits(
        JMockTest.class, null, new NullDiscoveryListener(),
        new IdentityTestUnitProcessor());
    assertFalse(actual.isEmpty());
  }

  public static class JUnit3Test extends TestCase {
    public void testStuff() {

    }
  }

  @Test
  public void shouldNotFindTestUnitsInNonCustomJUnit3Class() {
    final Collection<TestUnit> actual = this.testee.findTestUnits(
        JUnit3Test.class, null, new NullDiscoveryListener(),
        new IdentityTestUnitProcessor());
    assertTrue(actual.isEmpty());
  }

  @Test
  public void shouldApplyTestProcessorExactlyOnce() {
    this.testee.findTestUnits(JMockTest.class, null,
        new NullDiscoveryListener(), this.processor);

    verify(this.processor, times(1)).apply(any(TestUnit.class));

  }

  @Test
  public void shouldListOfAllTestUnitsToDiscoveryListener() {
    this.testee.findTestUnits(JMockTest.class, null, this.discoveryListener,
        new IdentityTestUnitProcessor());
    verify(this.discoveryListener, times(1)).receiveTests(
        anyListOf(TestUnit.class));
  }

}
