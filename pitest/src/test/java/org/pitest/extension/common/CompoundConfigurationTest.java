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
package org.pitest.extension.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassInfo;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestClassIdentifier;
import org.pitest.extension.TestSuiteFinder;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitFinder;
import org.pitest.internal.TestClass;

public class CompoundConfigurationTest {

  private CompoundConfiguration testee;

  @Mock
  private Configuration         childOne;
  @Mock
  private TestClassIdentifier   testIdOne;
  @Mock
  private TestUnitFinder        testFinderOne;
  @Mock
  private TestSuiteFinder       suiteFinderOne;

  @Mock
  private Configuration         childTwo;
  @Mock
  private TestClassIdentifier   testIdTwo;
  @Mock
  private TestUnitFinder        testFinderTwo;
  @Mock
  private TestSuiteFinder       suiteFinderTwo;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    when(this.childOne.testClassIdentifier()).thenReturn(this.testIdOne);
    when(this.childTwo.testClassIdentifier()).thenReturn(this.testIdTwo);
    when(this.childOne.testUnitFinder()).thenReturn(this.testFinderOne);
    when(this.childTwo.testUnitFinder()).thenReturn(this.testFinderTwo);
    when(this.childOne.testSuiteFinder()).thenReturn(this.suiteFinderOne);
    when(this.childTwo.testSuiteFinder()).thenReturn(this.suiteFinderTwo);
    this.testee = new CompoundConfiguration(Arrays.asList(this.childOne,
        this.childTwo));
  }

  @Test
  public void shouldFindTestsWhenAChildFindsTests() {
    TestUnit tu = Mockito.mock(TestUnit.class);
    when(this.testFinderTwo.findTestUnits(any(Class.class))).thenReturn(
        Arrays.asList(tu));
    when(this.testFinderOne.findTestUnits(any(Class.class))).thenReturn(
        Collections.<TestUnit> emptyList());
    assertEquals(Arrays.asList(tu),
        this.testee.testUnitFinder().findTestUnits(String.class));
  }

  @Test
  public void shouldFindNoTestsWhenAChildrenFindNoTests() {
    when(this.testFinderTwo.findTestUnits(any(Class.class))).thenReturn(
        Collections.<TestUnit> emptyList());
    when(this.testFinderOne.findTestUnits(any(Class.class))).thenReturn(
        Collections.<TestUnit> emptyList());
    assertEquals(Collections.<TestUnit> emptyList(), this.testee
        .testUnitFinder().findTestUnits(String.class));
  }

  @Test
  public void shouldFindSuiteClassesWhenAChildFindsSuiteClasses() {
    TestClass tc = new TestClass(String.class);
    when(this.suiteFinderOne.apply(any(TestClass.class))).thenReturn(
        Collections.<TestClass> emptyList());
    when(this.suiteFinderTwo.apply(any(TestClass.class))).thenReturn(
        Arrays.asList(tc));
    assertEquals(Arrays.asList(tc), this.testee.testSuiteFinder().apply(tc));
  }

  @Test
  public void shouldIdentifyClassAsATestClassWhenAChildIdentifiesItAsATest() {
    ClassInfo classInfoA = Mockito.mock(ClassInfo.class);
    ClassInfo classInfoB = Mockito.mock(ClassInfo.class);
    when(this.testIdOne.isATestClass(classInfoA)).thenReturn(true);
    when(this.testIdTwo.isATestClass(classInfoB)).thenReturn(true);

    assertTrue(this.testee.testClassIdentifier().isATestClass(classInfoA));
    assertTrue(this.testee.testClassIdentifier().isATestClass(classInfoB));
  }

  @Test
  public void shouldNotIdentifyClassAsATestClassWhenNoChildIdentifiesItAsATest() {
    ClassInfo classInfo = Mockito.mock(ClassInfo.class);
    when(this.testIdOne.isATestClass(classInfo)).thenReturn(false);
    when(this.testIdTwo.isATestClass(classInfo)).thenReturn(false);

    assertFalse(this.testee.testClassIdentifier().isATestClass(classInfo));
  }

}
