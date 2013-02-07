package org.pitest.mutationtest;

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
import org.pitest.extension.Configuration;
import org.pitest.extension.TestUnit;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.util.JavaAgent;

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
    final List<TestUnit> actual = this.testee.createMutationTestUnits(Arrays
        .asList(new ClassName("foo")));
    assertEquals(3, actual.size());
  }

  @Test
  public void shouldCreateNoUnitsWhenNoMutationsFound() {
    when(this.source.createMutations(any(ClassName.class))).thenReturn(
        Collections.<MutationDetails> emptyList());
    assertTrue(this.testee.createMutationTestUnits(
        Arrays.asList(new ClassName("foo"))).isEmpty());
  }

  private void assertCreatesOneTestUnitForTwoMutations() {
    final MutationDetails mutation1 = createDetails();
    final MutationDetails mutation2 = createDetails();
    when(this.source.createMutations(any(ClassName.class))).thenReturn(
        Arrays.asList(mutation1, mutation2));
    final List<TestUnit> actual = this.testee.createMutationTestUnits(Arrays
        .asList(new ClassName("foo")));
    assertEquals(1, actual.size());
  }

}
