package org.pitest.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class GlobTest {

  @Test
  public void shouldHandleEmptyStrings() {
    final Glob glob = new Glob("");
    assertThat(glob.matches("")).isTrue();
  }

  @Test
  public void shouldFindExactMatches() {
    final String value = "org.foo.foo";
    final Glob glob = new Glob(value);
    assertThat(glob.matches(value)).isTrue();
  }

  @Test
  public void shouldNotMatchNonMatchingStringWhenNoWildcardsPresent() {
    final String value = "org.foo.foo";
    final Glob glob = new Glob("org.foo");
    assertThat(glob.matches(value)).isFalse();
  }

  @Test
  public void shouldMatchEverythingAfterAStar() {
    final Glob glob = new Glob("org.foo.*");
    assertThat(glob.matches("org.foo.foo")).isTrue();
    assertThat(glob.matches("org.foo.")).isTrue();
    assertThat(glob.matches("org.foo.bar")).isTrue();
  }

  @Test
  public void shouldNotMatchIfContentDiffersBeforeAStar() {
    final Glob glob = new Glob("org.foo.*");
    assertThat(glob.matches("org.fo")).isFalse();
  }

  @Test
  public void shouldEscapeDotsInGeneratedRegex() {
    final Glob glob = new Glob("org.foo.bar");
    assertThat(glob.matches("orgafooabar")).isFalse();
  }

  @Test
  public void shouldSupportQuestionMarkWildCard() {
    final Glob glob = new Glob("org?foo?bar");
    assertThat(glob.matches("org.foo.bar")).isTrue();
    assertThat(glob.matches("orgafooabar")).isTrue();
  }

  @Test
  public void shouldEscapeEscapesInGeneratedRegex() {
    final Glob glob = new Glob("org.\\bar");
    assertThat(glob.matches("org.\\bar")).isTrue();
    assertThat(glob.matches("org.bar")).isFalse();
  }

  @Test
  public void shouldSupportMultipleWildcards() {
    final Glob glob = new Glob("foo*bar*car");
    assertThat(glob.matches("foo!!!bar!!!car")).isTrue();
    assertThat(glob.matches("foo!!!!!car")).isFalse();
  }

  @Test
  public void shouldBeCaseSensitive() {
    final Glob glob = new Glob("foo*bar*car");
    assertThat(glob.matches("foo!!!bar!!!car")).isTrue();
    assertThat(glob.matches("foo!!!Bar!!!car")).isFalse();
  }

  @Test
  public void matchesStringsWithPlusSign() {
    final Glob glob = new Glob("foo+bar+car");
    assertThat(glob.matches("foo+bar+car")).isTrue();
    assertThat(glob.matches("foo-Bar-car")).isFalse();
  }

  @Test
  public void shouldSupportDoubleStarPackageMatcher() {
    final Glob glob = new Glob("**.databinding.**.Foo");
    assertThat(glob.matches("databinding.Foo")).isTrue();
    assertThat(glob.matches("databinding.bar.Foo")).isTrue();
    assertThat(glob.matches("databinding.bar.car.Foo")).isTrue();
    assertThat(glob.matches("foo.databinding.Foo")).isTrue();
    assertThat(glob.matches("foo.car.databinding.bar.Foo")).isTrue();
    assertThat(glob.matches(".databinding.Foo")).isTrue();
    assertThat(glob.matches("databindingfoo.Foo")).isFalse();
    assertThat(glob.matches("foodatabinding.Foo")).isFalse();
    assertThat(glob.matches("databinding.fooFoo")).isFalse();
  }

  @Test
  public void escapesParentheses() {
    final Glob glob = new Glob("some () path");
    assertThat(glob.matches("some () path")).isTrue();
    assertThat(glob.matches("some path")).isFalse();
  }

  @Test
  public void escapesSquareBrackets() {
    final Glob glob = new Glob("some [] path");
    assertThat(glob.matches("some [] path")).isTrue();
    assertThat(glob.matches("some path")).isFalse();
  }

}
