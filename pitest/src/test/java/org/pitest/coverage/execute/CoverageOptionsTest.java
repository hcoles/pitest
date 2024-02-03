package org.pitest.coverage.execute;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.util.Verbosity.DEFAULT;

import java.util.Collections;

import org.junit.Test;
import org.pitest.mutationtest.config.TestPluginArguments;

public class CoverageOptionsTest {

  TestPluginArguments pitConfig = TestPluginArguments.defaults();

  CoverageOptions testee = new CoverageOptions(Collections.singletonList("*"), Collections.<String>emptyList(), this.pitConfig, DEFAULT);

  @Test
  public void shouldIncludeTargettedClasses() {
    this.testee = new CoverageOptions(Collections.singletonList("com.example.*"), Collections.<String>emptyList(), this.pitConfig, DEFAULT);

    assertThat(this.testee.getFilter().test("com.example.Foo")).isTrue();
  }

  @Test
  public void shouldExcludeExcludedClasses() {
    this.testee = new CoverageOptions(Collections.singletonList("com.example.*"), Collections.singletonList("com.example.NotMe"), this.pitConfig, DEFAULT);

    assertThat(this.testee.getFilter().test("com.example.Foo")).isTrue();
    assertThat(this.testee.getFilter().test("com.example.NotMe")).isFalse();
  }

  @Test
  public void shouldNotCoverJDKClassesWhenFilterIsBroad() {
    assertThat(this.testee.getFilter().test("java.lang.Integer")).isFalse();
  }

  @Test
  public void shouldNotCoverSunClassesWhenFilterIsBroad() {
    assertThat(this.testee.getFilter().test("sun.foo.Bar")).isFalse();
  }

  @Test
  public void shouldNotCoverDotSun() {
    assertThat(this.testee.getFilter().test("com.sun.dance")).isFalse();
  }

  @Test
  public void shouldCoverSunDance() {
    assertThat(this.testee.getFilter().test("com.sundance")).isTrue();
  }

  @Test
  public void shouldNotCoverJUnitWhenFilterIsBroad() {
    assertThat(this.testee.getFilter().test("org.junit.Bar")).isFalse();
  }

  @Test
  public void shouldNotCoverPitestBootWhenFilterIsBroad() {
    assertThat(this.testee.getFilter().test("org.pitest.boot.HotSwapAgent")).isFalse();
  }

  @Test
  public void shouldNotCoverPitestCoverageWhenFilterIsBroad() {
    assertThat(this.testee.getFilter().test("org.pitest.coverage.execute.Minion")).isFalse();
  }

  @Test
  public void shouldNotCoverPitestRelocWhenFilterIsBroad() {
    assertThat(this.testee.getFilter().test("org.pitest.reloc.Foo")).isFalse();
  }

}
