package org.pitest.mutationtest;

import static org.junit.Assert.assertEquals;
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
import org.pitest.coverage.CoverageDatabase;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestUnit;
import org.pitest.internal.ClassByteArraySource;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.filter.MutationFilterFactory;
import org.pitest.mutationtest.filter.UnfilteredMutationFilter;
import org.pitest.util.JavaAgent;


public class MutationTestBuilderTest {
  
  private MutationTestBuilder testee;
  
  private ReportOptions         data;
  
  private MutationConfig        mutationConfig;
  
  @Mock
  private MutationEngine engine;
  
  @Mock
  private Mutater mutater;
  
  @Mock
  private CoverageDatabase coverageDatabase;
    
  @Mock
  private MutationFilterFactory filterFactory;
  
  @Mock
  private ClassByteArraySource  source;

  @Mock
  private JavaAgent javaAgent;

  @Mock
  private Configuration configuration;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    data = new ReportOptions();
    mutationConfig = new MutationConfig(engine, Collections.<String>emptyList());
    when(engine.createMutator(source)).thenReturn(mutater);
    setupFilterFactoryToFilterNothing();
    testee = new MutationTestBuilder(mutationConfig, filterFactory, coverageDatabase, data, source, configuration, javaAgent);
  }
  
  private void setupFilterFactoryToFilterNothing() {
    when(filterFactory.createFilter()).thenReturn(UnfilteredMutationFilter.INSTANCE);
  }

  @Test
  public void shouldCreateSingleUnitPerClassWhenUnitSizeIsZero() {
    data.setMutationUnitSize(0);
    assertCreatesOneTestUnitForTwoMutations();
  }
  
  @Test
  public void shouldCreateSingleUnitPerClassWhenUnitSizeIsLessThanZero() {
    data.setMutationUnitSize(-1);
    assertCreatesOneTestUnitForTwoMutations();
  }

  @Test
  public void shouldCreateMultipleTestUnitsWhenUnitSizeIsLessThanNumberOfMutations() {
    data.setMutationUnitSize(1);
    when(mutater.findMutations(any(ClassName.class))).thenReturn(Arrays.asList(createDetails(),createDetails(),createDetails()));
    List<TestUnit> actual = testee.createMutationTestUnits(Arrays.asList(new ClassName("foo")));
    assertEquals(3,actual.size());
  }
  
  private void assertCreatesOneTestUnitForTwoMutations() {
    MutationDetails mutation1 = createDetails();
    MutationDetails mutation2 = createDetails();
    when(mutater.findMutations(any(ClassName.class))).thenReturn(Arrays.asList(mutation1,mutation2));
    List<TestUnit> actual = testee.createMutationTestUnits(Arrays.asList(new ClassName("foo")));
    assertEquals(1,actual.size());
  }
  
}
