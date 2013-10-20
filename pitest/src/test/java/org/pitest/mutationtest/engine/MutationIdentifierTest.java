package org.pitest.mutationtest.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pitest.mutationtest.LocationMother.aLocation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

public class MutationIdentifierTest {
  
  @Test
  public void shouldEqualSelf() {
    MutationIdentifier a = new MutationIdentifier(aLocation(), 1, "M" );
    assertTrue(a.equals(a));
  }
  
  @Test
  public void shouldBeUnEqualWhenIndexDiffers() {
    MutationIdentifier a = new MutationIdentifier(aLocation(), 1, "M" );
    MutationIdentifier b = new MutationIdentifier(aLocation(), 2, "M" );
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));
  }
  
  @Test
  public void shouldBeUnEqualWhenMutatorDiffers() {
    MutationIdentifier a = new MutationIdentifier(aLocation(), 1, "FOO" );
    MutationIdentifier b = new MutationIdentifier(aLocation(), 1, "BAR" );
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));
  }

  @Test
  public void shouldBeUnEqualWhenLocationDiffers() {
    MutationIdentifier a = new MutationIdentifier(aLocation().withClass("FOO"), 1, "M" );
    MutationIdentifier b = new MutationIdentifier(aLocation().withClass("BAR"), 1, "M" );
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));
  }

  
  @Test
  public void shouldHaveSymmetricEqulasImplementation() {
    MutationIdentifier a = new MutationIdentifier(aLocation(), 1, "M" );
    MutationIdentifier b = new MutationIdentifier(aLocation(), 1, "M" );
    assertTrue(a.equals(b));
    assertTrue(b.equals(a));
    assertTrue(a.hashCode() == b.hashCode());
  }
  
  @Test
  public void shouldMatchWhenObjectsAreEqual() {
    MutationIdentifier a = new MutationIdentifier(aLocation(), 1, "M" );
    MutationIdentifier b = new MutationIdentifier(aLocation(), 1, "M" );
    assertTrue(a.matches(b));
  }
  
  @Test
  public void shouldMatchWhenIndexesOverlap() {
    MutationIdentifier a = new MutationIdentifier(aLocation(), new HashSet<Integer>(Arrays.asList(1,2)), "M" );
    MutationIdentifier b = new MutationIdentifier(aLocation(), 1, "M" );
    assertTrue(a.matches(b));
  }
  
  @Test
  public void shouldNotMatchWhenIndexesDoNotOverlap() {
    MutationIdentifier a = new MutationIdentifier(aLocation(), new HashSet<Integer>(100,200), "M" );
    MutationIdentifier b = new MutationIdentifier(aLocation(), 1, "M" );
    assertFalse(a.matches(b));
  }
  
  @Test
  public void shouldNotMatchWhenMutatorsDiffer() {
    MutationIdentifier a = new MutationIdentifier(aLocation(), 1, "M" );
    MutationIdentifier b = new MutationIdentifier(aLocation(), 1, "XXXX" );
    assertFalse(a.matches(b));
  }
  
  
  @Test
  public void shouldNotMatchWhenIndexesDiffer() {
    MutationIdentifier a = new MutationIdentifier(aLocation(), 1, "M" );
    MutationIdentifier b = new MutationIdentifier(aLocation(), 100, "M" );
    assertFalse(a.matches(b));
  }
  
  @Test
  public void shouldNotMatchWhenLocationsDiffer() {
    MutationIdentifier a = new MutationIdentifier(aLocation().withMethodDesc("X"), 1, "M" );
    MutationIdentifier b = new MutationIdentifier(aLocation().withMethodDesc("Y"), 1, "M" );
    assertFalse(a.matches(b));
  }
  
  @Test
  public void shouldSortInConsistantOrder() {
    MutationIdentifier a = new MutationIdentifier(aLocation(), 1, "A" );
    MutationIdentifier b = new MutationIdentifier(aLocation(), 1, "Z" );
    MutationIdentifier c = new MutationIdentifier(aLocation(), 1, "AA" );
    MutationIdentifier d = new MutationIdentifier(aLocation(), 3, "AA" );
    MutationIdentifier e = new MutationIdentifier(aLocation().withMethod("a"), 3, "AA" );
    List<MutationIdentifier> mis = Arrays.asList(a,b,c,d,e);
    Collections.sort(mis);
    final List<MutationIdentifier> expectedOrder = Arrays.asList(e,a,c,d,b);
    assertEquals(expectedOrder, mis);
    mis = Arrays.asList(e,b,d,a,c);
    Collections.sort(mis);
    assertEquals(expectedOrder, mis);
    
  }
  
}
