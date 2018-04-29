package org.pitest.mutationtest.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pitest.mutationtest.LocationMother.aLocation;
import static org.pitest.mutationtest.LocationMother.aMutationId;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class MutationIdentifierTest {

  @Test
  public void shouldEqualSelf() {
    final MutationIdentifier a = aMutationId().withIndex(1).withMutator("M").build();
    assertTrue(a.equals(a));
  }

  @Test
  public void shouldBeUnEqualWhenIndexDiffers() {
    final MutationIdentifier a = aMutationId().withIndex(1).build();
    final MutationIdentifier b = aMutationId().withIndex(2).build();
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));
  }

  @Test
  public void shouldBeUnEqualWhenMutatorDiffers() {
    final MutationIdentifier a = aMutationId().withMutator("FOO").build();
    final MutationIdentifier b = aMutationId().withMutator("BAR").build();
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));
  }

  @Test
  public void shouldBeUnEqualWhenLocationDiffers() {
    final MutationIdentifier a = aMutationId().withLocation(
        aLocation().withMethod("FOO")).build();
    final MutationIdentifier b = aMutationId().withLocation(
        aLocation().withMethod("BAR")).build();
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));
  }

  @Test
  public void shouldHaveSymmetricEqulasImplementation() {
    final MutationIdentifier a = aMutationId().withIndex(1).withMutator("M").build();
    final MutationIdentifier b = aMutationId().withIndex(1).withMutator("M").build();
    assertTrue(a.equals(b));
    assertTrue(b.equals(a));
    assertTrue(a.hashCode() == b.hashCode());
  }

  @Test
  public void shouldMatchWhenObjectsAreEqual() {
    final MutationIdentifier a = aMutationId().build();
    final MutationIdentifier b = aMutationId().build();
    assertTrue(a.matches(b));
  }

  @Test
  public void shouldMatchWhenIndexesOverlap() {
    final MutationIdentifier a = new MutationIdentifier(aLocation().build(),
        new HashSet<>(Arrays.asList(1, 2)), "M");
    final MutationIdentifier b = new MutationIdentifier(aLocation().build(), 1, "M");
    assertTrue(a.matches(b));
  }

  @Test
  public void shouldNotMatchWhenIndexesDoNotOverlap() {
    final MutationIdentifier a = new MutationIdentifier(aLocation().build(),
        new HashSet<Integer>(100, 200), "M");
    final MutationIdentifier b = new MutationIdentifier(aLocation().build(), 1, "M");
    assertFalse(a.matches(b));
  }

  @Test
  public void shouldNotMatchWhenMutatorsDiffer() {
    final MutationIdentifier a = aMutationId().withMutator("A").build();
    final MutationIdentifier b = aMutationId().withMutator("XXXX").build();
    assertFalse(a.matches(b));
  }

  @Test
  public void shouldNotMatchWhenIndexesDiffer() {
    final MutationIdentifier a = aMutationId().withIndex(1).build();
    final MutationIdentifier b = aMutationId().withIndex(100).build();
    assertFalse(a.matches(b));
  }

  @Test
  public void shouldNotMatchWhenLocationsDiffer() {
    final MutationIdentifier a = new MutationIdentifier(aLocation()
        .withMethodDescription("X").build(), 1, "M");
    final MutationIdentifier b = new MutationIdentifier(aLocation()
        .withMethodDescription("Y").build(), 1, "M");
    assertFalse(a.matches(b));
  }

  @Test
  public void shouldSortInConsistantOrder() {
    final MutationIdentifier a = aMutationId().withIndex(1).withMutator("A").build();
    final MutationIdentifier b = aMutationId().withIndex(1).withMutator("Z").build();
    final MutationIdentifier c = aMutationId().withIndex(1).withMutator("AA").build();
    final MutationIdentifier d = aMutationId().withIndex(3).withMutator("AA").build();
    final MutationIdentifier e = aMutationId()
        .withLocation(aLocation().withMethod("a")).withIndex(3)
        .withMutator("AA").build();
    List<MutationIdentifier> mis = Arrays.asList(a, b, c, d, e);
    Collections.sort(mis);
    final List<MutationIdentifier> expectedOrder = Arrays.asList(e, a, c, d, b);
    assertEquals(expectedOrder, mis);
    mis = Arrays.asList(e, b, d, a, c);
    Collections.sort(mis);
    assertEquals(expectedOrder, mis);

  }

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(MutationIdentifier.class).verify();
  }

}
