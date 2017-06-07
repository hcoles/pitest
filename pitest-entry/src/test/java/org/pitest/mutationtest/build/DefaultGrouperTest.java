package org.pitest.mutationtest.build;

import static org.junit.Assert.assertEquals;
import static org.pitest.mutationtest.LocationMother.aMutationId;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.LocationMother;
import org.pitest.mutationtest.LocationMother.LocationBuilder;
import org.pitest.mutationtest.engine.MutationDetails;

public class DefaultGrouperTest {

  private DefaultGrouper testee;

  @Test
  public void shouldCreateSingleUnitPerClassWhenUnitSizeIsZero() {
    makeTesteeWithUnitSizeOf(0);
    assertCreatesOneUnitForTwoMutations();
  }

  @Test
  public void shouldCreateSingleUnitPerClassWhenUnitSizeIsLessThanZero() {
    makeTesteeWithUnitSizeOf(-1);
    assertCreatesOneUnitForTwoMutations();
  }

  @Test
  public void shouldCreateMultipleTestUnitsWhenUnitSizeIsLessThanNumberOfMutations() {
    makeTesteeWithUnitSizeOf(1);
    final List<List<MutationDetails>> actual = this.testee.groupMutations(
        Arrays.asList(ClassName.fromString("foo")), Arrays.asList(
            createDetails("foo"), createDetails("foo"), createDetails("foo")));

    assertEquals(3, actual.size());
  }

  private void assertCreatesOneUnitForTwoMutations() {
    final MutationDetails mutation1 = createDetails("foo");
    final MutationDetails mutation2 = createDetails("foo");
    final List<List<MutationDetails>> actual = this.testee.groupMutations(null,
        Arrays.asList(mutation1, mutation2));
    assertEquals(1, actual.size());
  }

  private void makeTesteeWithUnitSizeOf(final int i) {
    this.testee = new DefaultGrouper(i);
  }

  public static MutationDetails createDetails(final String clazz) {
    LocationBuilder lb = LocationMother.aLocation().withClass(
        ClassName.fromString(clazz));
    return new MutationDetails(aMutationId().withLocation(lb).build(), "",
        "desc", 42, 0);
  }

}
