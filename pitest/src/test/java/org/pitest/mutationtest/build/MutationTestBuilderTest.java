package org.pitest.mutationtest.build;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.pitest.mutationtest.report.MutationTestResultMother.createDetails;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.MutationAnalysisUnit;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.NullAnalyser;
import org.pitest.mutationtest.build.MutationSource;
import org.pitest.mutationtest.build.MutationTestBuilder;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.process.JavaAgent;
import org.pitest.testapi.Configuration;

public class MutationTestBuilderTest {

  private MutationTestBuilder testee;

  private ReportOptions       data;

  private MutationConfig      mutationConfig;

  @Mock
  private MutationEngine      engine;

  @Mock
  private MutationSource      source;

  @Mock
  private JavaAgent           javaAgent;

  @Mock
  private Configuration       configuration;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.data = new ReportOptions();
    this.mutationConfig = new MutationConfig(this.engine,
        Collections.<String> emptyList());
    this.testee = new MutationTestBuilder(null, this.mutationConfig,
        new NullAnalyser(), this.source, this.data, this.configuration,
        this.javaAgent);
  }

  @Test
  public void shouldCreateSingleUnitPerClassWhenUnitSizeIsZero() {
    this.data.setMutationUnitSize(0);
    assertCreatesOneTestUnitForTwoMutations();
  }

  @Test
  public void shouldCreateSingleUnitPerClassWhenUnitSizeIsLessThanZero() {
    this.data.setMutationUnitSize(-1);
    assertCreatesOneTestUnitForTwoMutations();
  }

  @Test
  public void shouldCreateMultipleTestUnitsWhenUnitSizeIsLessThanNumberOfMutations() {
    this.data.setMutationUnitSize(1);
    when(this.source.createMutations(any(ClassName.class))).thenReturn(
        Arrays.asList(createDetails(), createDetails(), createDetails()));
    final List<MutationAnalysisUnit> actual = this.testee
        .createMutationTestUnits(Arrays.asList(new ClassName("foo")));
    assertEquals(3, actual.size());
  }

  @Test
  public void shouldCreateNoUnitsWhenNoMutationsFound() {
    when(this.source.createMutations(any(ClassName.class))).thenReturn(
        Collections.<MutationDetails> emptyList());
    assertTrue(this.testee.createMutationTestUnits(
        Arrays.asList(new ClassName("foo"))).isEmpty());
  }

  @Test
  public void shouldOrderLargestUnitFirst() {
    final MutationDetails mutation1 = createDetails();
    final MutationDetails mutation2 = createDetails();
    final ClassName foo = ClassName.fromString("foo");
    final ClassName bar = ClassName.fromString("bar");
    when(this.source.createMutations(foo)).thenReturn(Arrays.asList(mutation1));
    when(this.source.createMutations(bar)).thenReturn(
        Arrays.asList(mutation1, mutation2));
    final List<MutationAnalysisUnit> actual = this.testee
        .createMutationTestUnits(Arrays.asList(foo, bar));
    assertTrue(actual.get(0).priority() > actual.get(1).priority());
  }

  private void assertCreatesOneTestUnitForTwoMutations() {
    final MutationDetails mutation1 = createDetails();
    final MutationDetails mutation2 = createDetails();
    when(this.source.createMutations(any(ClassName.class))).thenReturn(
        Arrays.asList(mutation1, mutation2));
    final List<MutationAnalysisUnit> actual = this.testee
        .createMutationTestUnits(Arrays.asList(new ClassName("foo")));
    assertEquals(1, actual.size());
  }

}
