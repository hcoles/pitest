package org.pitest.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GlobTest {

  @Test
  public void shouldHandleEmptyStrings() {
    final Glob glob = new Glob("");
    assertTrue(glob.matches(""));
  }

  @Test
  public void shouldFindExactMatches() {
    final String value = "org.foo.foo";
    final Glob glob = new Glob(value);
    assertTrue(glob.matches(value));
  }

  @Test
  public void shouldNotMatchNonMatchingStringWhenNoWildcardsPresent() {
    final String value = "org.foo.foo";
    final Glob glob = new Glob("org.foo");
    assertFalse(glob.matches(value));
  }

  @Test
  public void shouldMatchEverythingAfterAStar() {
    final Glob glob = new Glob("org.foo.*");
    assertTrue(glob.matches("org.foo.foo"));
    assertTrue(glob.matches("org.foo."));
    assertTrue(glob.matches("org.foo.bar"));
  }

  @Test
  public void shouldNotMatchIfContentDiffersBeforeAStar() {
    final Glob glob = new Glob("org.foo.*");
    assertFalse(glob.matches("org.fo"));
  }

  @Test
  public void shouldEscapeDotsInGeneratedRegex() {
    final Glob glob = new Glob("org.foo.bar");
    assertFalse(glob.matches("orgafooabar"));
  }

  @Test
  public void shouldSupportQuestionMarkWildCard() {
    final Glob glob = new Glob("org?foo?bar");
    assertTrue(glob.matches("org.foo.bar"));
    assertTrue(glob.matches("orgafooabar"));
  }

  @Test
  public void shouldEscapeEscapesInGeneratedRegex() {
    final Glob glob = new Glob("org.\\bar");
    assertTrue(glob.matches("org.\\bar"));
    assertFalse(glob.matches("org.bar"));
  }

  @Test
  public void shouldSupportMultipleWildcards() {
    final Glob glob = new Glob("foo*bar*car");
    assertTrue(glob.matches("foo!!!bar!!!car"));
    assertFalse(glob.matches("foo!!!!!car"));
  }

  @Test
  public void shouldBeCaseSensitice() {
    final Glob glob = new Glob("foo*bar*car");
    assertTrue(glob.matches("foo!!!bar!!!car"));
    assertFalse(glob.matches("foo!!!Bar!!!car"));
  }

}
