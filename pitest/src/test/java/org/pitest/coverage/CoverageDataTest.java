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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pitest.coverage.CoverageMother.aBlockLocation;
import static org.pitest.coverage.CoverageMother.aCoverageResult;
import static org.pitest.mutationtest.LocationMother.aLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassInfoMother;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.CodeSource;
import org.pitest.coverage.CoverageMother.BlockLocationBuilder;
import org.pitest.coverage.CoverageMother.CoverageResultBuilder;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.testapi.Description;

public class CoverageDataTest {

  private CoverageData    testee;

  @Mock
  private CodeSource      code;

  @Mock
  private LineMap         lm;

  private final ClassName foo = ClassName.fromString("foo");
  private final ClassName bar = ClassName.fromString("bar");

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    when(this.lm.mapLines(any(ClassName.class))).thenReturn(
        new HashMap<BlockLocation, Set<Integer>>());
    this.testee = new CoverageData(this.code, this.lm);
  }

  @Test
  public void shouldReturnNoTestsWhenNoTestsCoverALine() {
    when(this.lm.mapLines(any(ClassName.class))).thenReturn(
        new HashMap<BlockLocation, Set<Integer>>());
    final ClassLine line = new ClassLine("foo", 1);
    assertEquals(Collections.emptyList(),
        this.testee.getTestsForClassLine(line));
  }

  @Test
  public void shouldStoreExecutionTimesOfTests() {

    int line = 1;
    int time = 42;

    BlockLocationBuilder block = aBlockLocation().withLocation(
        aLocation().withClass(this.foo));
    when(this.lm.mapLines(any(ClassName.class))).thenReturn(
        makeCoverageMapForBlock(block, line));

    CoverageResultBuilder cr = aCoverageResult().withVisitedBlocks(
        block.build(1)).withExecutionTime(time);

    this.testee.calculateClassCoverage(cr.build());

    assertEquals(Arrays.asList(42), FCollection.map(
        this.testee.getTestsForClassLine(new ClassLine(this.foo, line)),
        testInfoToExecutionTime()));
  }

  @Test
  public void shouldReportNumberOfCoveredLinesWhenNoneCovered() {
    assertEquals(0, this.testee.getNumberOfCoveredLines(Collections
        .singletonList(ClassName.fromString("foo"))));
  }

  @Test
  public void shouldReportNumberOfCoveredLinesWhenSomeCovered() {

    BlockLocationBuilder block = aBlockLocation().withLocation(
        aLocation().withClass(this.foo));
    when(this.lm.mapLines(any(ClassName.class))).thenReturn(
        makeCoverageMapForBlock(block, 101, 300));

    CoverageResultBuilder cr = aCoverageResult().withVisitedBlocks(
        block.build(1));

    this.testee.calculateClassCoverage(cr.build());

    assertEquals(2, this.testee.getNumberOfCoveredLines(Collections
        .singletonList(this.foo)));
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

  @SuppressWarnings("unchecked")
  @Test
  public void shouldReturnNonZeroCoverageIdWhenTestsCoverClass() {

    final ClassName foo = ClassName.fromString("Foo");
    final ClassInfo ci = ClassInfoMother.make(foo);

    when(this.code.getClassInfo(any(Collection.class))).thenReturn(
        Collections.singletonList(ci));

    BlockLocationBuilder block = aBlockLocation().withLocation(
        aLocation().withClass(foo));
    HashMap<BlockLocation, Set<Integer>> map = makeCoverageMapForBlock(block,
        42);
    when(this.lm.mapLines(any(ClassName.class))).thenReturn(map);
    this.testee.calculateClassCoverage(aCoverageResult().withVisitedBlocks(
        block.build(1)).build());

    assertThat(this.testee.getCoverageIdForClass(foo).longValue())
        .isNotEqualTo(0);

  }

  @Test
  public void shouldProvideEmptyBlockCoverageListWhenNoCoverage() {
    assertEquals(Collections.emptyList(), this.testee.createCoverage());
  }

  @Test
  public void shouldProvideCoverageListWhenCoverageRecorded() {

    BlockLocationBuilder block = aBlockLocation().withLocation(
        aLocation().withClass(this.foo));
    CoverageResultBuilder cr = aCoverageResult().withVisitedBlocks(
        block.build(1));

    this.testee.calculateClassCoverage(cr.build());

    this.testee.calculateClassCoverage(makeCoverageResult("foo", "fooTest", 0,
        1));
    final BlockCoverage actual = this.testee.createCoverage().get(0);
    assertEquals(block.build(), actual.getBlock());
    assertThat(actual.getTests()).contains("FooTest.fooTest");
  }

  @Test
  public void shouldProvideListOfClassesForSourceFile() {

    final ClassInfo fooClass = ClassInfoMother.make(this.foo, "foo.java");
    final ClassInfo barClass = ClassInfoMother.make(this.bar, "bar.java");
    final Collection<ClassInfo> classes = Arrays.asList(fooClass, barClass);
    when(this.code.getCode()).thenReturn(classes);

    this.testee = new CoverageData(this.code, this.lm);

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

    this.testee = new CoverageData(this.code, this.lm);

    assertEquals(Arrays.asList(foo1Class),
        this.testee.getClassesForFile("foo.java", "a.b.c"));
  }

  @Test
  public void shouldIncludeAllCoveredLinesInCoverageSummary() {

    BlockLocationBuilder block = aBlockLocation();
    when(this.code.getCodeUnderTestNames()).thenReturn(
        Collections.singleton(block.build().getLocation().getClassName()));
    when(this.lm.mapLines(any(ClassName.class))).thenReturn(
        makeCoverageMapForBlock(block, 1, 2, 3, 4));

    CoverageResultBuilder cr = aCoverageResult().withVisitedBlocks(
        block.build(1));

    this.testee.calculateClassCoverage(cr.build());

    CoverageSummary actual = this.testee.createSummary();
    assertEquals(4, actual.getNumberOfCoveredLines());
  }

  private static F<TestInfo, Integer> testInfoToExecutionTime() {
    return new F<TestInfo, Integer>() {
      @Override
      public Integer apply(final TestInfo a) {
        return a.getTime();
      }
    };
  }

  private static F<TestInfo, String> testInfoToString() {
    return new F<TestInfo, String>() {
      @Override
      public String apply(final TestInfo a) {
        return a.getName();
      }
    };
  }

  private CoverageResult makeCoverageResult(final String clazz,
      final String testName, final int time, final int block) {
    return makeCoverageResult(clazz, new Description(testName), time, block,
        true);
  }

  private CoverageResult makeCoverageResult(final String clazz,
      final Description desc, final int time, final int block,
      final boolean testPassed) {
    return new CoverageResult(desc, time, testPassed,
        makeCoverage(clazz, block));
  }

  private Collection<BlockLocation> makeCoverage(final String clazz,
      final int block) {
    final BlockLocation cs = new BlockLocation(Location.location(
        ClassName.fromString(clazz), MethodName.fromString("foo"), "V"), block);

    return Collections.singleton(cs);
  }

  private HashMap<BlockLocation, Set<Integer>> makeCoverageMapForBlock(
      BlockLocationBuilder blocks, Integer... lines) {
    HashMap<BlockLocation, Set<Integer>> map = new HashMap<BlockLocation, Set<Integer>>();
    Set<Integer> s = new HashSet<Integer>();
    s.addAll(Arrays.asList(lines));
    map.put(blocks.build(), s);
    return map;
  }

}
