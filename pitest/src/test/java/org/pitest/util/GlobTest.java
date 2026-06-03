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


  @Test
  public void issue705PathWhiteListPrefixHat1() {
    final Glob glob = new Glob("/home/user/a^b/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/a^b/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/a^b/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/a^b/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  @Test
  public void issue705PathWhiteListPrefixHat2() {
    final Glob glob = new Glob("/home/user/a^/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/a^/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/a^/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/a^/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  @Test
  public void issue705PathWhiteListPrefixHat3() {
    final Glob glob = new Glob("/home/user/^b/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/^b/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/^b/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/^b/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  @Test
  public void issue705PathWhiteListPrefixPlus1() {
    final Glob glob = new Glob("/home/user/a+b/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/a+b/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/a+b/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/a+b/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  @Test
  public void issue705PathWhiteListPrefixPlus2() {
    final Glob glob = new Glob("/home/user/ab+/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/ab+/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/ab+/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/ab+/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  @Test
  public void issue705PathWhiteListPrefixPlus3() {
    final Glob glob = new Glob("/home/user/+ab/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/+ab/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/+ab/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/+ab/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  @Test
  public void issue705PathWhiteListPrefixPlus4() {
    final Glob glob = new Glob("/home/user/a++b/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/a++b/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/a++b/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/a++b/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  @Test
  public void issue705PathWhiteListPrefixCurlyBraces1() {
    final Glob glob = new Glob("/home/user/p{s}q/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/p{s}q/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/p{s}q/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/p{s}q/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  @Test
  public void issue705PathWhiteListPrefixCurlyBraces2() {
    final Glob glob = new Glob("/home/user/p{}q/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/p{}q/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/p{}q/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/p{}q/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  @Test
  public void issue705PathWhiteListPrefixCurlyBraces3() {
    final Glob glob = new Glob("/home/user/p{/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/p{/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/p{/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/p{/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  /**
   * PASSING
   */
  @Test
  public void pathWhiteListPrefixCurlyBracesOK() {
    final Glob glob = new Glob("/home/user/p}/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/p}/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/p}/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/p}/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  @Test
  public void issue705PathWhiteListPrefixParentheses1() {
    final Glob glob = new Glob("/home/user/f(x)/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/f(x)/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/f(x)/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/f(x)/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  @Test
  public void issue705PathWhiteListPrefixParentheses2() {
    final Glob glob = new Glob("/home/user/f()/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/f()/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/f()/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/f()/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  @Test
  public void issue705PathWhiteListPrefixParentheses3() {
    final Glob glob = new Glob("/home/user/f(/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/f(/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/f(/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/f(/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  @Test
  public void issue705PathWhiteListPrefixParentheses4() {
    final Glob glob = new Glob("/home/user/x)/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/x)/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/x)/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/x)/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  @Test
  public void issue705PathWhiteListPrefixSqBracks1() {
    final Glob glob = new Glob("/home/user/E[X]/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/E[X]/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/E[X]/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/E[X]/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  @Test
  public void issue705PathWhiteListPrefixSqBracks2() {
    final Glob glob = new Glob("/home/user/E[]/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/E[]/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/E[]/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/E[]/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  @Test
  public void issue705PathWhiteListPrefixSqBracks3() {
    final Glob glob = new Glob("/home/user/E[/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/E[/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/E[/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/E[/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  /**
   * PASSING
   */
  @Test
  public void pathWhiteListPrefixSqBracksOK() {
    final Glob glob = new Glob("/home/user/X]/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/X]/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/X]/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/X]/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  /**
   * PASSING:
   * Equivalent shouldNotMatchIfContentDiffersBeforeAStar(), above.
   */
  @Test
  public void packageWhiteListPrefix() {
    final Glob glob = new Glob("org.apache.commons.cli.*");
    assertTrue(glob.matches("org.apache.commons.cli."));
    assertTrue(glob.matches("org.apache.commons.cli.something"));
    assertTrue(glob.matches("org.apache.commons.cli.something.else"));
    assertFalse(glob.matches("org.apache.commons.clis"));
    assertFalse(glob.matches("org.apache.commons.cli"));
    assertFalse(glob.matches("com.apache.commons.cli"));
  }

  /**
   * PASSING:
   * Coming from org.pitest.mutationtest.config.ReportOptions
   */
  @Test
  public void pathWhiteListPrefix() {
    final Glob glob = new Glob("/home/user/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  /**
   * PASSING
   */
  @Test
  public void pathWhiteListPrefixWindows() {
    final Glob glob = new Glob("C:\\user\\commons-cli\\target\\classes");
    assertFalse(glob.matches("C:\\user\\commons-cli\\target\\test-classes"));
    assertTrue(glob.matches("C:\\user\\commons-cli\\target\\classes"));
    assertFalse(glob.matches("C:\\user\\.m2\\repository\\junit\\junit\\4.12\\junit-4.12.jar"));
  }

  /**
   * PASSING
   */
  @Test
  public void pathWhiteListPrefixDollarSignOK1() {
    final Glob glob = new Glob("/home/user/a$b/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/a$b/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/a$b/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/a$b/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  /**
   * PASSING
   */
  @Test
  public void pathWhiteListPrefixDollarSignOK2() {
    final Glob glob = new Glob("/home/user/a$/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/a$/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/a$/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/a$/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  /**
   * PASSING
   */
  @Test
  public void pathWhiteListPrefixDollarSignOK3() {
    final Glob glob = new Glob("/home/user/$b/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/$b/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/$b/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/$b/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  /**
   * PASSING
   */
  @Test
  public void pathWhiteListPrefixQuestionMarkOK1() {
    final Glob glob = new Glob("/home/user/a?b/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/a?b/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/a?b/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/a?b/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  /**
   * PASSING
   */
  @Test
  public void oathWhiteListPrefixQuestionMarkOK2() {
    final Glob glob = new Glob("/home/user/?b/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/?b/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/?b/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/?b/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  /**
   * PASSING
   */
  @Test
  public void pathWhiteListPrefixQuestionMarkOK3() {
    final Glob glob = new Glob("/home/user/a?/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/a?/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/a?/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/a?/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  /**
   * PASSING
   */
  @Test
  public void pathWhiteListPrefixCommaOK1() {
    final Glob glob = new Glob("/home/user/a,b/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/a,b/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/a,b/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/a,b/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  /**
   * PASSING
   */
  @Test
  public void pathWhiteListPrefixCommaOK2() {
    final Glob glob = new Glob("/home/user/a,/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/a,/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/a,/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/a,/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  /**
   * PASSING
   */
  @Test
  public void pathWhiteListPrefixCommaOK3() {
    final Glob glob = new Glob("/home/user/,b/commons-cli/target/classes");
    assertFalse(glob.matches("/home/user/,b/commons-cli/target/test-classes"));
    assertTrue(glob.matches("/home/user/,b/commons-cli/target/classes"));
    assertFalse(glob.matches("/home/user/,b/.m2/repository/junit/junit/4.12/junit-4.12.jar"));
  }

  /**
   * PASSING
   */
  @Test
  public void pathWhiteListQuotingOK1() {
    final Glob glob = new Glob("C:\\E\\commons-cli\\Q\\target\\classes");
    assertFalse(glob.matches("C:\\E\\commons-cli\\Q\\target\\test-classes"));
    assertTrue(glob.matches("C:\\E\\commons-cli\\Q\\target\\classes"));
    assertFalse(glob.matches("C:\\E\\.m2\\repository\\Q\\junit\\junit\\4.12\\junit-4.12.jar"));
  }

  /**
   * PASSING
   */
  @Test
  public void pathWhiteListQuotingOK2() {
    final Glob glob = new Glob("C:\\Q\\commons-cli\\E\\target\\classes");
    assertFalse(glob.matches("C:\\Q\\commons-cli\\E\\target\\test-classes"));
    assertTrue(glob.matches("C:\\Q\\commons-cli\\E\\target\\classes"));
    assertFalse(glob.matches("C:\\Q\\.m2\\repository\\E\\junit\\junit\\4.12\\junit-4.12.jar"));
  }

}
