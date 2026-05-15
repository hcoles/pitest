package org.pitest.mutationtest.build;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.pitest.mutationtest.LocationMother.aLocation;
import static org.pitest.mutationtest.LocationMother.aMutationId;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.config.ExecutionMode;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.incremental.NullHistory;

public class MutationTestBuilderTest {

  private MutationTestBuilder testee;

  @Mock
  private MutationSource      source;

  @Mock
  private WorkerFactory       wf;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    makeTesteeWithUnitSizeOf(0);
  }

  @Test
  public void shouldCreateSingleUnitPerClassWhenUnitSizeIsZero() {
    makeTesteeWithUnitSizeOf(0);
    assertCreatesOneTestUnitForTwoMutations();
  }

  @Test
  public void shouldCreateSingleUnitPerClassWhenUnitSizeIsLessThanZero() {
    makeTesteeWithUnitSizeOf(-1);
    assertCreatesOneTestUnitForTwoMutations();
  }

  @Test
  public void shouldCreateMultipleTestUnitsWhenUnitSizeIsLessThanNumberOfMutations() {
    makeTesteeWithUnitSizeOf(1);
    when(this.source.createMutations(any(ClassName.class))).thenReturn(
        Arrays.asList(createDetails("foo"), createDetails("foo"),
            createDetails("foo")));
    final List<MutationAnalysisUnit> actual = this.testee
        .createMutationTestUnits(Arrays.asList(ClassName.fromString("foo")));
    assertThat(actual).hasSize(3);
  }

  @Test
  public void shouldCreateNoUnitsWhenNoMutationsFound() {
    when(this.source.createMutations(any(ClassName.class))).thenReturn(
        Collections.<MutationDetails> emptyList());
    assertThat(this.testee.createMutationTestUnits(
        Arrays.asList(ClassName.fromString("foo")))).isEmpty();
  }

  @Test
  public void shouldOrderLargestUnitFirst() {
    final MutationDetails mutation1 = createDetails("foo");
    final MutationDetails mutation2 = createDetails("bar");
    final ClassName foo = ClassName.fromString("foo");
    final ClassName bar = ClassName.fromString("bar");
    when(this.source.createMutations(foo)).thenReturn(Arrays.asList(mutation1));
    when(this.source.createMutations(bar)).thenReturn(
        Arrays.asList(mutation2, mutation2));
    final List<MutationAnalysisUnit> actual = this.testee
        .createMutationTestUnits(Arrays.asList(foo, bar));
    assertThat(actual.get(0).priority()).isGreaterThan(actual.get(1).priority());
  }

  private void assertCreatesOneTestUnitForTwoMutations() {
    final MutationDetails mutation1 = createDetails("foo");
    final MutationDetails mutation2 = createDetails("foo");
    when(this.source.createMutations(any(ClassName.class))).thenReturn(
        Arrays.asList(mutation1, mutation2));
    final List<MutationAnalysisUnit> actual = this.testee
        .createMutationTestUnits(Arrays.asList(ClassName.fromString("foo")));
    assertThat(actual).hasSize(1);
  }

  @Test
  public void shouldApplyProjectFilterAcrossAllMutations() {
    when(this.source.createMutations(any(ClassName.class))).thenReturn(
        Arrays.asList(createDetails("foo"), createDetails("foo")));
    this.testee = new MutationTestBuilder(ExecutionMode.NORMAL, this.wf, new NullHistory(),
        this.source, new DefaultGrouper(0), filterAllMutants());

    assertThat(this.testee.createMutationTestUnits(Arrays.asList(ClassName.fromString("foo"))))
            .isEmpty();
  }

  private ProjectMutationInterceptor filterAllMutants() {
    return new ProjectMutationInterceptor() {

      @Override
      public Collection<MutationDetails> intercept(Collection<MutationDetails> mutations) {
        return List.of();
      }

      @Override
      public InterceptorType type() {
        return InterceptorType.FILTER;
      }
    };
  }

  private void makeTesteeWithUnitSizeOf(int unitSize) {
    this.testee = new MutationTestBuilder(ExecutionMode.NORMAL, this.wf, new NullHistory(),
        this.source, new DefaultGrouper(unitSize), CompoundProjectMutationInterceptor.passThrough());
  }

  public static MutationDetails createDetails(String clazz) {
    return new MutationDetails(aMutationId().withLocation(aLocation(clazz))
        .build(), "", "desc", 42, 0);
  }

}
