package org.pitest.mutationtest.engine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

public class MutationIdentifierTest {
  
  @Test
  public void shouldEqualSelf() {
    MutationIdentifier a = new MutationIdentifier(Location.location("foo", "bar", "()V"), 1, "M" );
    assertTrue(a.equals(a));
  }
  
  @Test
  public void shouldBeUnEqualWhenIndexDiffers() {
    MutationIdentifier a = new MutationIdentifier(Location.location("foo", "bar", "()V"), 1, "M" );
    MutationIdentifier b = new MutationIdentifier(Location.location("foo", "bar", "()V"), 2, "M" );
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));
  }
  
  @Test
  public void shouldBeUnEqualWhenMutatorDiffers() {
    MutationIdentifier a = new MutationIdentifier(Location.location("foo", "bar", "()V"), 1, "FOO" );
    MutationIdentifier b = new MutationIdentifier(Location.location("foo", "bar", "()V"), 1, "BAR" );
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));
  }

  @Test
  public void shouldBeUnEqualWhenLocationDiffers() {
    MutationIdentifier a = new MutationIdentifier(Location.location("FOO", "bar", "()V"), 1, "M" );
    MutationIdentifier b = new MutationIdentifier(Location.location("BAR", "bar", "()V"), 1, "M" );
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));
  }

  
  @Test
  public void shouldHaveSymmetricEqulasImplementation() {
    MutationIdentifier a = new MutationIdentifier(Location.location("foo", "bar", "()V"), 1, "M" );
    MutationIdentifier b = new MutationIdentifier(Location.location("foo", "bar", "()V"), 1, "M" );
    assertTrue(a.equals(b));
    assertTrue(b.equals(a));
    assertTrue(a.hashCode() == b.hashCode());
  }
  
  @Test
  public void shouldMatchWhenObjectsAreEqual() {
    MutationIdentifier a = new MutationIdentifier(Location.location("foo", "bar", "()V"), 1, "M" );
    MutationIdentifier b = new MutationIdentifier(Location.location("foo", "bar", "()V"), 1, "M" );
    assertTrue(a.matches(b));
  }
  
  @Test
  public void shouldMatchWhenIndexesOverlap() {
    MutationIdentifier a = new MutationIdentifier(Location.location("foo", "bar", "()V"), new HashSet<Integer>(Arrays.asList(1,2)), "M" );
    MutationIdentifier b = new MutationIdentifier(Location.location("foo", "bar", "()V"), 1, "M" );
    assertTrue(a.matches(b));
  }
  
  @Test
  public void shouldNotMatchWhenIndexesDoNotOverlap() {
    MutationIdentifier a = new MutationIdentifier(Location.location("foo", "bar", "()V"), new HashSet<Integer>(100,200), "M" );
    MutationIdentifier b = new MutationIdentifier(Location.location("foo", "bar", "()V"), 1, "M" );
    assertFalse(a.matches(b));
  }
  
  @Test
  public void shouldNotMatchWhenMutatorsDiffer() {
    MutationIdentifier a = new MutationIdentifier(Location.location("foo", "bar", "()V"), 1, "M" );
    MutationIdentifier b = new MutationIdentifier(Location.location("foo", "bar", "()V"), 1, "XXXX" );
    assertFalse(a.matches(b));
  }
  
  
  @Test
  public void shouldNotMatchWhenIndexesDiffer() {
    MutationIdentifier a = new MutationIdentifier(Location.location("foo", "bar", "()V"), 1, "M" );
    MutationIdentifier b = new MutationIdentifier(Location.location("foo", "bar", "()V"), 100, "M" );
    assertFalse(a.matches(b));
  }
  
  @Test
  public void shouldNotMatchWhenLocationsDiffer() {
    MutationIdentifier a = new MutationIdentifier(Location.location("foo", "bar", "()V"), 1, "M" );
    MutationIdentifier b = new MutationIdentifier(Location.location("foo", "bar", "XXXX"), 1, "M" );
    assertFalse(a.matches(b));
  }
  
}
