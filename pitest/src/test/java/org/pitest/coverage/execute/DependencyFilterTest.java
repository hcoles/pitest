package org.pitest.coverage.execute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.dependency.DependencyExtractor;
import org.pitest.functional.predicate.Predicate;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestUnit;

public class DependencyFilterTest {

  private DependencyFilter    testee;

  @Mock
  private DependencyExtractor extractor;

  private TestUnit            aTestUnit;

  private TestUnit            anotherTestUnit;

  private List<TestUnit>      tus;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    this.aTestUnit = makeTestUnit(new Description("foo", String.class));
    this.anotherTestUnit = makeTestUnit(new Description("bar", Integer.class));

    this.testee = new DependencyFilter(this.extractor, null);
    this.tus = Arrays.asList(this.aTestUnit, this.anotherTestUnit);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldNotPerformAnalysisWhenDependencyDistanceIsLessThan0()
      throws IOException {
    when(this.extractor.getMaxDistance()).thenReturn(-1);
    final List<TestUnit> actual = this.testee
        .filterTestsByDependencyAnalysis(this.tus);
    assertSame(this.tus, actual);
    verify(this.extractor, never()).extractCallDependenciesForPackages(
        anyString(), any(Predicate.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldReturnOnlyTestUnitsForClassesWithinReach()
      throws IOException {
    when(
        this.extractor.extractCallDependenciesForPackages(eq(this.aTestUnit
            .getDescription().getFirstTestClass()), any(Predicate.class)))
            .thenReturn(Arrays.asList("foo"));
    when(
        this.extractor.extractCallDependenciesForPackages(
            eq(this.anotherTestUnit.getDescription().getFirstTestClass()),
            any(Predicate.class))).thenReturn(Collections.<String> emptyList());

    assertEquals(Arrays.asList(this.aTestUnit),
        this.testee.filterTestsByDependencyAnalysis(this.tus));

  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldNotRecalculateDependenciesForAlreadyAnalysedClasses()
      throws IOException {

    when(
        this.extractor.extractCallDependenciesForPackages(eq(this.aTestUnit
            .getDescription().getFirstTestClass()), any(Predicate.class)))
            .thenReturn(Arrays.asList("foo"));

    this.tus = Arrays.asList(this.aTestUnit, this.aTestUnit);

    this.testee.filterTestsByDependencyAnalysis(this.tus);
    verify(this.extractor, times(1)).extractCallDependenciesForPackages(
        eq(this.aTestUnit.getDescription().getFirstTestClass()),
        any(Predicate.class));
  }

  private TestUnit makeTestUnit(final Description d) {
    return new TestUnit() {

      @Override
      public void execute(final ClassLoader loader, final ResultCollector rc) {

      }

      @Override
      public Description getDescription() {
        return d;
      }

    };
  }
}
