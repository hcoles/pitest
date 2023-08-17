package org.pitest.mutationtest.build;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.pitest.mutationtest.LocationMother.aLocation;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.coverage.BlockLocation;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.TestInfo;
import java.util.Optional;
import java.util.stream.Collectors;

import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class DefaultTestPrioritiserTest {

  private DefaultTestPrioritiser testee;

  @Mock
  private CoverageDatabase       coverage;

  @Mock
  private ClassByteArraySource   source;

  private final ClassName        foo = ClassName.fromString("foo");

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    this.testee = new DefaultTestPrioritiser(this.coverage);
  }

  @Test
  public void shouldAssignTestsForRelevantLineToGeneratedMutations() {
    final List<TestInfo> expected = makeTestInfos(0);
    when(this.coverage.getTestsForBlockLocation(any(BlockLocation.class))).thenReturn(
        expected);
    final List<TestInfo> actual = this.testee.assignTests(makeMutation("foo"));
    assertEquals(expected, actual);
  }

  @Test
  public void shouldPrioritiseTestsByExecutionTime() {
    final List<TestInfo> unorderedTests = makeTestInfos(10000, 100, 1000, 1);
    when(this.coverage.getTestsForBlockLocation(any(BlockLocation.class))).thenReturn(
        unorderedTests);
    final List<TestInfo> actual = this.testee.assignTests(makeMutation("foo"));

    assertThat(actual.stream().map(toTime())).contains(1, 100, 1000, 10000);
  }

  private Function<TestInfo, Integer> toTime() {
    return a -> a.getTime();
  }

  private List<TestInfo> makeTestInfos(final Integer... times) {
    return Arrays.stream(times)
            .map(timeToTestInfo())
            .collect(Collectors.toList());
  }

  private Function<Integer, TestInfo> timeToTestInfo() {
    return a -> new TestInfo("foo", "bar" + a, a, Optional.<ClassName> empty(), 0);
  }

  private MutationDetails makeMutation(final String method) {
    final MutationIdentifier id = new MutationIdentifier(aLocation()
        .withClass(this.foo).withMethod(method).build(), 0, "mutator");
    return new MutationDetails(id, "file", "desc", 1, 2);
  }

}
