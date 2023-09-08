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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassInfoMother;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.classpath.CodeSource;
import org.pitest.coverage.CoverageMother.BlockLocationBuilder;
import org.pitest.coverage.CoverageMother.CoverageResultBuilder;
import org.pitest.mutationtest.engine.Location;
import org.pitest.testapi.Description;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.pitest.coverage.CoverageMother.aBlockLocation;
import static org.pitest.coverage.CoverageMother.aCoverageResult;
import static org.pitest.mutationtest.LocationMother.aLocation;

public class CoverageDataTest {

  private CoverageData    testee;

  @Mock
  private CodeSource      code;

  @Mock
  private LineMap         lm;

  private final ClassName foo = ClassName.fromString("foo");

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    when(this.lm.mapLines(any(ClassName.class))).thenReturn(
        new HashMap<>());
    when(this.code.findTestee(any())).thenReturn(Optional.empty());
    this.testee = new CoverageData(this.code, this.lm);
  }

  @Test
  public void shouldReturnNoTestsWhenNoTestsCoverALine() {
    when(this.lm.mapLines(any(ClassName.class))).thenReturn(
        new HashMap<>());
    assertThat(this.testee.getCoveredLines(ClassName.fromString("foo"))).isEmpty();
  }

  @Test
  public void shouldReportNumberOfCoveredLinesWhenNoneCovered() {
    assertThat(this.testee.getCoveredLines(ClassName.fromString("foo"))).isEmpty();
  }

  @Test
  public void shouldReportNumberOfCoveredLinesWhenSomeCovered() {

    final BlockLocationBuilder block = aBlockLocation().withLocation(
        aLocation().withClass(this.foo));
    when(this.lm.mapLines(any(ClassName.class))).thenReturn(
        makeCoverageMapForBlock(block, 101, 300));

    final CoverageResultBuilder cr = aCoverageResult().withVisitedBlocks(
        block.build(1));

    this.testee.calculateClassCoverage(cr.build());

    assertThat(this.testee.getCoveredLines(this.foo)).hasSize(2);
  }

  @Test
  public void shouldReturnNotTestsWhenNoTestsCoverClass() {
    assertTrue(this.testee.getTestsForClass(this.foo).isEmpty());
  }

  @Test
  @Ignore("temp ignore")
  public void shouldReturnUniqueTestsForClassWhenSomeTestsCoverClass() {
    this.testee.calculateClassCoverage(makeCoverageResult("foo", "fooTest", 0,
        1));
    this.testee.calculateClassCoverage(makeCoverageResult("foo", "fooTest", 0,
        2));
    this.testee.calculateClassCoverage(makeCoverageResult("foo", "fooTest2", 0,
        2));

    List<String> actual = this.testee.getTestsForClass(this.foo).stream()
            .map(TestInfo::getName)
            .collect(Collectors.toList());

    assertThat(actual).containsExactlyInAnyOrder("fooTest", "fooTest2");
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
  public void shouldReturnCoverageIdOf0WhenNoTestsCoverClass() {
    assertEquals(0,
        this.testee.getCoverageIdForClass(ClassName.fromString("unknown"))
        .longValue());
  }

  @Test
  public void shouldReturnNonZeroCoverageIdWhenTestsCoverClass() {

    final ClassName foo = ClassName.fromString("Foo");
    final ClassInfo ci = ClassInfoMother.make(foo);

    when(this.code.fetchClassHashes(any(Collection.class))).thenReturn(
        Collections.singletonList(ci));

    final BlockLocationBuilder block = aBlockLocation().withLocation(
        aLocation().withClass(foo));
    final HashMap<BlockLocation, Set<Integer>> map = makeCoverageMapForBlock(block,
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

    final BlockLocationBuilder block = aBlockLocation().withLocation(
        aLocation().withClass(this.foo));
    final CoverageResultBuilder cr = aCoverageResult().withVisitedBlocks(
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

    ClassTree fooClass = treeFor(com.example.a.b.c.Foo.class);
    ClassTree barClass = treeFor(com.example.a.b.c.Bar.class);
    when(this.code.codeTrees()).thenReturn(Stream.of(fooClass, barClass));

    this.testee = new CoverageData(this.code, this.lm);

    assertThat(this.testee.getClassesForFile("Bar.java", "com.example.a.b.c"))
            .containsExactly(new ClassLines(barClass.name(), Collections.emptySet()));
  }

  @Test
  public void shouldMatchPackageWhenFindingSources() {
    final ClassTree foo1Class = treeFor(com.example.a.b.c.Foo.class);
    final ClassTree foo2Class = treeFor(com.example.d.e.f.Foo.class);
    final Collection<ClassTree> classes = Arrays.asList(foo1Class, foo2Class);

    when(this.code.codeTrees()).thenReturn(classes.stream());

    this.testee = new CoverageData(this.code, this.lm);

    assertThat(this.testee.getClassesForFile("Foo.java", "com.example.a.b.c"))
            .containsExactly(new ClassLines(foo1Class.name(), Collections.emptySet()));

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
        ClassName.fromString(clazz), "foo", "V"), block);

    return Collections.singleton(cs);
  }

  private HashMap<BlockLocation, Set<Integer>> makeCoverageMapForBlock(
      BlockLocationBuilder blocks, Integer... lines) {
    final HashMap<BlockLocation, Set<Integer>> map = new HashMap<>();
    final Set<Integer> s = new HashSet<>();
    s.addAll(Arrays.asList(lines));
    map.put(blocks.build(), s);
    return map;
  }


  ClassTree treeFor(Class<?> clazz) {
    return ClassTree.fromBytes(ClassloaderByteArraySource.fromContext()
            .getBytes(ClassName.fromClass(clazz).asJavaName()).get());
  }

}
