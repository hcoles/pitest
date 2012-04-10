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
import org.pitest.Description;
import org.pitest.dependency.DependencyExtractor;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;
import org.pitest.functional.predicate.Predicate;

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

    aTestUnit = makeTestUnit(new Description("foo", String.class));
    anotherTestUnit = makeTestUnit(new Description("bar", Integer.class));

    testee = new DependencyFilter(extractor, null);
    tus = Arrays.asList(aTestUnit, anotherTestUnit);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldNotPerformAnalysisWhenDependencyDistanceIsLessThan0()
      throws IOException {
    when(extractor.getMaxDistance()).thenReturn(-1);
    List<TestUnit> actual = testee.filterTestsByDependencyAnalysis(tus);
    assertSame(tus, actual);
    verify(extractor, never()).extractCallDependenciesForPackages(anyString(),
        any(Predicate.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldReturnOnlyTestUnitsForClassesWithinReach()
      throws IOException {
    when(
        this.extractor.extractCallDependenciesForPackages(eq(aTestUnit
            .getDescription().getFirstTestClass()), any(Predicate.class)))
        .thenReturn(Arrays.asList("foo"));
    when(
        this.extractor.extractCallDependenciesForPackages(eq(anotherTestUnit
            .getDescription().getFirstTestClass()), any(Predicate.class)))
        .thenReturn(Collections.<String> emptyList());

    assertEquals(Arrays.asList(aTestUnit),
        testee.filterTestsByDependencyAnalysis(tus));

  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldNotRecalculateDependenciesForAlreadyAnalysedClasses()
      throws IOException {

    when(
        this.extractor.extractCallDependenciesForPackages(eq(aTestUnit
            .getDescription().getFirstTestClass()), any(Predicate.class)))
        .thenReturn(Arrays.asList("foo"));

    tus = Arrays.asList(aTestUnit, aTestUnit);

    testee.filterTestsByDependencyAnalysis(tus);
    verify(extractor, times(1)).extractCallDependenciesForPackages(
        eq(aTestUnit.getDescription().getFirstTestClass()),
        any(Predicate.class));
  }

  private TestUnit makeTestUnit(final Description d) {
    return new TestUnit() {

      public void execute(ClassLoader loader, ResultCollector rc) {

      }

      public Description getDescription() {
        return d;
      }

    };
  }
}
