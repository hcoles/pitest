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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassInfoMother;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.CodeSource;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.testapi.Description;

public class CoverageDataTest {

  private CoverageData    testee;

  @Mock
  private CodeSource      code;

  private final ClassName foo = ClassName.fromString("foo");

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new CoverageData(this.code);
  }

  @Test
  public void shouldReturnNoTestsWhenNoTestsCoverALine() {
    final ClassLine line = new ClassLine("foo", 1);
    assertEquals(Collections.emptyList(),
        this.testee.getTestsForClassLine(line));
  }

  @Test
  public void shouldReturnOnlyTestsThatCoverGivenLine() {
    final int lineNumber = 1;
    final int executionTime = 100;

    final ClassLine line = new ClassLine("foo", lineNumber);

    this.testee.calculateClassCoverage(makeCoverageResult("foo", "fooTest",
        executionTime, lineNumber));
    this.testee.calculateClassCoverage(makeCoverageResult("foo", "fooTestToo",
        executionTime, lineNumber));
    this.testee.calculateClassCoverage(makeCoverageResult("foo", "fooTestMiss",
        executionTime, lineNumber + 1));
    this.testee.calculateClassCoverage(makeCoverageResult("bar", "BarTest",
        executionTime, lineNumber));

    assertEquals(Arrays.asList("fooTest", "fooTestToo"), FCollection.map(
        this.testee.getTestsForClassLine(line), testInfoToString()));
  }

  @Test
  public void shouldStoreExecutionTimesOfTests() {
    this.testee.calculateClassCoverage(makeCoverageResult("foo", "fooTest", 42,
        1));
    this.testee.calculateClassCoverage(makeCoverageResult("foo", "fooTestToo",
        43, 2));

    assertEquals(Arrays.asList(42), FCollection.map(
        this.testee.getTestsForClassLine(new ClassLine("foo", 1)),
        testInfoToExecutionTime()));
    assertEquals(Arrays.asList(43), FCollection.map(
        this.testee.getTestsForClassLine(new ClassLine("foo", 2)),
        testInfoToExecutionTime()));
  }

  @Test
  public void shouldReportNumberOfCoveredLinesWhenNoneCovered() {
    assertEquals(0, this.testee.getNumberOfCoveredLines(Collections
        .singletonList(ClassName.fromString("foo"))));
  }

  @Test
  public void shouldReportNumberOfCoveredLinesWhenSomeCovered() {
    this.testee.calculateClassCoverage(makeCoverageResult("foo", "fooTest", 0,
        1));
    this.testee.calculateClassCoverage(makeCoverageResult("foo", "fooTest", 0,
        2));
    assertEquals(2, this.testee.getNumberOfCoveredLines(Collections
        .singletonList(ClassName.fromString("foo"))));
  }

  @Test
  public void shouldReturnNotTestsWhenNoTestsCoverClass() {
    assertTrue(this.testee.getTestsForClass(this.foo).isEmpty());
  }

  @Test
  public void shouldReturnUniqueTestsForClassWhenSomeTestsCoverClass() {
    this.testee.calculateClassCoverage(makeCoverageResult("foo", "fooTest", 0,
        1));
    this.testee.calculateClassCoverage(makeCoverageResult("foo", "fooTest", 0,
        2));
    this.testee.calculateClassCoverage(makeCoverageResult("foo", "fooTest2", 0,
        2));
    assertEquals(Arrays.asList("fooTest", "fooTest2"), FCollection.map(
        this.testee.getTestsForClass(this.foo), testInfoToString()));
  }

  @Test
  public void shouldReportAGreenSuiteWhenNoTestHasFailed() {
    this.testee.calculateClassCoverage(makeCoverageResult("foo", "fooTest", 42,
        1));
    assertTrue(this.testee.allTestsGreen());
  }

  @Test
  public void shouldNotReportAGreenSuiteWhenATestHasFailed() {
    this.testee.calculateClassCoverage(makeCoverageResult("foo",
        new Description("fooTest"), 42, 1, false));
    assertFalse(this.testee.allTestsGreen());
  }

  @Test
  public void shouldProvideAccessToClassData() {
    final Collection<ClassName> classes = Arrays.asList(ClassName
        .fromString("foo"));
    this.testee.getClassInfo(classes);
    verify(this.code).getClassInfo(classes);
  }

  @Test
  public void shouldReturnCoverageIdOf0WhenNoTestsCoverClass() {
    assertEquals(0,
        this.testee.getCoverageIdForClass(ClassName.fromString("unknown"))
            .longValue());
  }

  @Test
  public void shouldReturnNonZeroCoverageIdWhenTestsCoverClass() {
    final ClassName fooTest = ClassName.fromString("FooTest");
    final ClassInfo ci = ClassInfoMother.make(fooTest);
    when(this.code.getClassInfo(Collections.singleton(fooTest))).thenReturn(
        Collections.singletonList(ci));
    this.testee.calculateClassCoverage(makeCoverageResult("foo",
        new Description("fooTest", fooTest.asJavaName()), 0, 1, true));
    assertFalse(this.testee.getCoverageIdForClass(ClassName.fromString("foo"))
        .longValue() == 0);
  }

  @Test
  public void shouldProvideEmptyLineCoverageListWhenNoCoverage() {
    assertEquals(Collections.emptyList(), this.testee.createLineCoverage());
  }

  @Test
  public void shouldProvideLineCoverageListWhenCoverageRecorded() {
    final ClassLine fooLine1 = new ClassLine("foo", 1);
    this.testee.calculateClassCoverage(makeCoverageResult("foo", "fooTest", 0,
        1));
    this.testee.calculateClassCoverage(makeCoverageResult("foo", "fooTest2", 0,
        1));
    final LineCoverage actual = this.testee.createLineCoverage().get(0);
    assertEquals(fooLine1, actual.getClassLine());
    assertThat(actual.getTests(), hasItems("fooTest", "fooTest2"));
  }

  @Test
  public void shouldProvideListOfClassesForSourceFile() {
    final ClassName foo = ClassName.fromString("foo");
    final ClassName bar = ClassName.fromString("bar");
    final ClassInfo fooClass = ClassInfoMother.make(foo, "foo.java");
    final ClassInfo barClass = ClassInfoMother.make(bar, "bar.java");
    final Collection<ClassInfo> classes = Arrays.asList(fooClass, barClass);
    when(this.code.getCode()).thenReturn(classes);

    assertEquals(Arrays.asList(barClass),
        this.testee.getClassesForFile("bar.java", ""));
  }

  @Test
  public void shouldMatchPackageWhenFindingSources() {
    final ClassName foo1 = ClassName.fromString("a.b.c.foo");
    final ClassName foo2 = ClassName.fromString("d.e.f.foo");
    final ClassInfo foo1Class = ClassInfoMother.make(foo1, "foo.java");
    final ClassInfo foo2Class = ClassInfoMother.make(foo2, "foo.java");
    final Collection<ClassInfo> classes = Arrays.asList(foo1Class, foo2Class);
    when(this.code.getCode()).thenReturn(classes);

    assertEquals(Arrays.asList(foo1Class),
        this.testee.getClassesForFile("foo.java", "a.b.c"));
  }

  @Test
  public void shouldIncludeAllCoveredLinesInCoverageSummary() {
    this.testee.calculateClassCoverage(makeCoverageResult("foo", "fooTest", 0,
        1));
    this.testee.calculateClassCoverage(makeCoverageResult("bar", "barTest", 0,
        1));
    CoverageSummary actual = testee.createSummary();
    assertEquals(2, actual.getNumberOfCoveredLines());
  }
  
  private static F<TestInfo, Integer> testInfoToExecutionTime() {
    return new F<TestInfo, Integer>() {
      public Integer apply(final TestInfo a) {
        return a.getTime();
      }
    };
  }

  private static F<TestInfo, String> testInfoToString() {
    return new F<TestInfo, String>() {
      public String apply(final TestInfo a) {
        return a.getName();
      }
    };
  }

  private CoverageResult makeCoverageResult(final String clazz,
      final String testName, final int time, final int lineNumber) {
    return makeCoverageResult(clazz, new Description(testName), time,
        lineNumber, true);
  }

  private CoverageResult makeCoverageResult(final String clazz,
      final Description desc, final int time, final int lineNumber,
      final boolean testPassed) {
    return new CoverageResult(desc, time, testPassed, makeCoverage(clazz,
        lineNumber));
  }

  private Collection<ClassStatistics> makeCoverage(final String clazz,
      final int lineNumber) {
    final ClassStatistics cs = new ClassStatistics(clazz);
    cs.registerLineVisit(lineNumber);
    return Collections.singleton(cs);
  }

}
