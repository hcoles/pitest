package org.pitest.coverage.execute;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.Test;
import org.pitest.mutationtest.config.TestPluginArguments;

public class CoverageOptionsTest {
  
  TestPluginArguments pitConfig = TestPluginArguments.defaults();
  
  CoverageOptions testee = new CoverageOptions(Collections.singletonList("*"), Collections.<String>emptyList(), pitConfig, false, 0);

  @Test
  public void shouldIncludeTargettedClasses() {
    testee = new CoverageOptions(Collections.singletonList("com/example/*"), Collections.<String>emptyList(), pitConfig, false, 0);

    assertThat(testee.getFilter().apply("com/example/Foo")).isTrue();
  }  
  
  @Test
  public void shouldExcludeExcludedClasses() {
    testee = new CoverageOptions(Collections.singletonList("com/example/*"), Collections.singletonList("com/example/NotMe"), pitConfig, false, 0);

    assertThat(testee.getFilter().apply("com/example/Foo")).isTrue();
    assertThat(testee.getFilter().apply("com/example/NotMe")).isFalse();    
  }   
  
  @Test
  public void shouldNotCoverJDKClassesWhenFilterIsBroad() {
    assertThat(testee.getFilter().apply("java/lang/Integer")).isFalse();
  }
  
  @Test
  public void shouldNotCoverSunClassesWhenFilterIsBroad() {
    assertThat(testee.getFilter().apply("sun/foo/Bar")).isFalse();
  }
  
  @Test
  public void shouldNotCoverJUnitWhenFilterIsBroad() {
    assertThat(testee.getFilter().apply("sun/foo/Bar")).isFalse();
  }
  
  @Test
  public void shouldNotCoverPitestBootWhenFilterIsBroad() {
    assertThat(testee.getFilter().apply("org/pitest/boot/HotSwapAgent")).isFalse();
  }

  @Test
  public void shouldNotCoverPitestCoverageWhenFilterIsBroad() {
    assertThat(testee.getFilter().apply("org/pitest/coverage/execute/Minion")).isFalse();
  }
  
  @Test
  public void shouldNotCoverPitestRelocWhenFilterIsBroad() {
    assertThat(testee.getFilter().apply("org/pitest/reloc/Foo")).isFalse();
  }  
  
}
