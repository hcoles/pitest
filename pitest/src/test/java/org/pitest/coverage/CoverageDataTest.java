/*
 * Copyright 2012 Henry Coles
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

package org.pitest.coverage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.Description;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassInfoMother;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.CodeSource;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.coverage.execute.CoverageResult;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.instrument.ClassLine;


public class CoverageDataTest {
  
  private CoverageData testee;
  
  @Mock
  private CodeSource code;
  
  private ClassName foo = ClassName.fromString("foo");
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    testee = new CoverageData(code);
  }

  @Test
  public void shouldReturnNoTestsWhenNoTestsCoverALine() {
    ClassLine line = new ClassLine("foo",1);
    assertEquals(Collections.emptyList(),testee.getTestsForClassLine(line));
  }
  
  @Test
  public void shouldReturnOnlyTestsThatCoverGivenLine() {
    int lineNumber = 1;
    int executionTime = 100;
    
    ClassLine line = new ClassLine("foo",lineNumber);

    testee.calculateClassCoverage(makeCoverageResult("foo","fooTest", executionTime, lineNumber));
    testee.calculateClassCoverage(makeCoverageResult("foo","fooTestToo", executionTime, lineNumber));
    testee.calculateClassCoverage(makeCoverageResult("foo","fooTestMiss", executionTime, lineNumber+1));
    testee.calculateClassCoverage(makeCoverageResult("bar","BarTest", executionTime, lineNumber));
    
    assertEquals(Arrays.asList("fooTest","fooTestToo"), FCollection.map(testee.getTestsForClassLine(line), testInfoToString()));
  }
  
  @Test
  public void shouldStoreExecutionTimesOfTests() { 
    testee.calculateClassCoverage(makeCoverageResult("foo","fooTest", 42, 1));
    testee.calculateClassCoverage(makeCoverageResult("foo","fooTestToo", 43, 2));

    assertEquals(Arrays.asList(42), FCollection.map(testee.getTestsForClassLine(new ClassLine("foo",1)), testInfoToExecutionTime()));
    assertEquals(Arrays.asList(43), FCollection.map(testee.getTestsForClassLine(new ClassLine("foo",2)), testInfoToExecutionTime()));
  }
  
  @Test
  public void shouldReportNumberOfCoveredLinesWhenNoneCovered() {
    assertEquals(0,testee.getNumberOfCoveredLines(Collections.singletonList(ClassName.fromString("foo"))));
  }
  
  @Test
  public void shouldReportNumberOfCoveredLinesWhenSomeCovered() {
    testee.calculateClassCoverage(makeCoverageResult("foo","fooTest", 0, 1));
    testee.calculateClassCoverage(makeCoverageResult("foo","fooTest", 0, 2));
    assertEquals(2,testee.getNumberOfCoveredLines(Collections.singletonList(ClassName.fromString("foo"))));
  }
  
  @Test
  public void shouldReturnNotTestsWhenNoTestsCoverClass() {
    assertTrue(testee.getTestsForClass(foo).isEmpty());
  }
  
  @Test
  public void shouldReturnUniqueTestsForClassWhenSomeTestsCoverClass() {
    testee.calculateClassCoverage(makeCoverageResult("foo","fooTest", 0, 1));
    testee.calculateClassCoverage(makeCoverageResult("foo","fooTest", 0, 2));
    testee.calculateClassCoverage(makeCoverageResult("foo","fooTest2", 0, 2));
    assertEquals(Arrays.asList("fooTest","fooTest2"),FCollection.map(testee.getTestsForClass(foo),testInfoToString()));
  }
  
  
  @Test
  public void shouldReportAGreenSuiteWhenNoTestHasFailed() {
    testee.calculateClassCoverage(makeCoverageResult("foo","fooTest", 42, 1));
    assertTrue(testee.allTestsGreen());
  }
  
  @Test
  public void shouldNotReportAGreenSuiteWhenATestHasFailed() {
    testee.calculateClassCoverage(makeCoverageResult("foo",new Description("fooTest"), 42, 1, false));
    assertFalse(testee.allTestsGreen());
  }
  
  @Test
  public void shouldProvideAccessToClassData() {
    Collection<ClassName> classes = Arrays.asList(ClassName.fromString("foo"));
    testee.getClassInfo(classes);
    verify(this.code).getClassInfo(classes);
  }
  
  @Test
  public void shouldReturnCoverageIdOf0WhenNoTestsCoverClass() {
    assertEquals(0,testee.getCoverageIdForClass(ClassName.fromString("unknown")).longValue());
  }
  
  @Test
  public void shouldReturnNonZeroCoverageIdWhenTestsCoverClass() {
    ClassName fooTest = ClassName.fromString("FooTest");
    ClassInfo ci = ClassInfoMother.make(fooTest);
    when(this.code.getClassInfo(Collections.singleton(fooTest))).thenReturn(Collections.singletonList(ci));
    testee.calculateClassCoverage(makeCoverageResult("foo", new Description("fooTest",fooTest.asJavaName()), 0, 1, true));
    assertFalse(testee.getCoverageIdForClass(ClassName.fromString("foo")).longValue() == 0);
  }
  
  private static F<TestInfo, Integer> testInfoToExecutionTime() {
    return new F<TestInfo, Integer> () {
      public Integer apply(TestInfo a) {
        return a.getTime();
      }
    };
  }

  private static F<TestInfo, String> testInfoToString() {
    return new F<TestInfo, String> () {
      public String apply(TestInfo a) {
        return a.getName();
      }
    };
  }
  
  private CoverageResult makeCoverageResult(String clazz, String testName, int time, int lineNumber) {
    return  makeCoverageResult(clazz,new Description(testName),time,lineNumber,true);
  }

  private CoverageResult makeCoverageResult(String clazz, Description desc, int time, int lineNumber, boolean testPassed) {
    return new CoverageResult(desc, time,testPassed, makeCoverage(clazz, lineNumber));
  }

  private Collection<ClassStatistics> makeCoverage(String clazz, int lineNumber) {
    ClassStatistics cs = new ClassStatistics(clazz);
    cs.registerLineVisit(lineNumber);
    return Collections.singleton(cs);
  }


}
