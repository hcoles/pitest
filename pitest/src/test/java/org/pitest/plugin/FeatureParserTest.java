  package org.pitest.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.util.Optional;

public class FeatureParserTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  FeatureParser testee = new FeatureParser();

  @Test
  public void shouldRejectInputNotStartingWithPlusOrMinus() {
    this.thrown.expect(RuntimeException.class);
    final FeatureSetting actual = parse("FOO");
    assertThat(actual.addsFeature()).isFalse();
  }

  @Test
  public void shouldEnableFeaturesStartingWithPlus() {
    final FeatureSetting actual = parse("+FOO");
    assertThat(actual.addsFeature()).isTrue();
    assertThat(actual.removesFeature()).isFalse();
  }

  @Test
  public void shouldDisableFeaturesStartingWithMinus() {
    final FeatureSetting actual = parse("-FOO");
    assertThat(actual.addsFeature()).isFalse();
    assertThat(actual.removesFeature()).isTrue();
  }

  @Test
  public void shouldParseFeatureNameWhenNoConfig() {
    final FeatureSetting actual = parse("-FOO");
    assertThat(actual.feature()).isEqualTo("FOO");
  }

  @Test
  public void shouldParseFeatureNameWhenLeadingWhitespace() {
    final FeatureSetting actual = parse("   -FOO");
    assertThat(actual.feature()).isEqualTo("FOO");
  }

  @Test
  public void shouldParseFeatureNameWhenTrailingWhitespace() {
    final FeatureSetting actual = parse("-FOO   ");
    assertThat(actual.feature()).isEqualTo("FOO");
  }

  @Test
  public void shouldParseFeatureNameWhenEmptyConfig() {
    final FeatureSetting actual = parse("+BAR()");
    assertThat(actual.feature()).isEqualTo("BAR");
  }

  @Test
  public void shouldParseSingleConfigValues() {
    final FeatureSetting actual = parse("+BAR(name[hello])");
    assertThat(actual.getString("name")).isEqualTo(Optional.ofNullable("hello"));
  }

  @Test
  public void shouldParseMultipleConfigValues() {
    final FeatureSetting actual = parse("+BAR(name[hello]size[42])");
    assertThat(actual.getString("name")).isEqualTo(Optional.ofNullable("hello"));
    assertThat(actual.getString("size")).isEqualTo(Optional.ofNullable("42"));
  }


  @Test
  public void shouldParseListValues() {
    final FeatureSetting actual = parse("+BAR(things[1] things[2] things[3] things[4] size[42])");
    assertThat(actual.getList("things")).contains("1","2","3","4");
  }


  private FeatureSetting parse(String dsl) {
    final List<FeatureSetting> actual = this.testee.parseFeatures(Collections.singletonList(dsl));
    return actual.get(0);
  }

}
