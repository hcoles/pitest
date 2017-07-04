  package org.pitest.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.pitest.functional.Option;

public class FeatureParserTest {
  
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  FeatureParser testee = new FeatureParser();
  
  @Test
  public void shouldRejectInputNotStartingWithPlusOrMinus() {
    thrown.expect(RuntimeException.class);
    FeatureSetting actual = parse("FOO");
    assertThat(actual.addsFeature()).isFalse();
  }
  
  @Test
  public void shouldEnableFeaturesStartingWithPlus() {
    FeatureSetting actual = parse("+FOO");
    assertThat(actual.addsFeature()).isTrue();
    assertThat(actual.removesFeature()).isFalse();        
  }

  @Test
  public void shouldDisableFeaturesStartingWithMinus() {
    FeatureSetting actual = parse("-FOO");
    assertThat(actual.addsFeature()).isFalse();
    assertThat(actual.removesFeature()).isTrue();    
  }
  
  @Test
  public void shouldParseFeatureNameWhenNoConfig() {
    FeatureSetting actual = parse("-FOO");
    assertThat(actual.feature()).isEqualTo("FOO");
  }
  
  @Test
  public void shouldParseFeatureNameWhenLeadingWhitespace() {
    FeatureSetting actual = parse("   -FOO");
    assertThat(actual.feature()).isEqualTo("FOO");
  }
  
  @Test
  public void shouldParseFeatureNameWhenTrailingWhitespace() {
    FeatureSetting actual = parse("-FOO   ");
    assertThat(actual.feature()).isEqualTo("FOO");
  }
  
  @Test
  public void shouldParseFeatureNameWhenEmptyConfig() {
    FeatureSetting actual = parse("+BAR()");
    assertThat(actual.feature()).isEqualTo("BAR");
  }
  
  @Test
  public void shouldParseSingleConfigValues() {
    FeatureSetting actual = parse("+BAR(name[hello])");
    assertThat(actual.getString("name")).isEqualTo(Option.some("hello"));
  }
  
  @Test
  public void shouldParseMultipleConfigValues() {
    FeatureSetting actual = parse("+BAR(name[hello]size[42])");
    assertThat(actual.getString("name")).isEqualTo(Option.some("hello"));
    assertThat(actual.getString("size")).isEqualTo(Option.some("42"));
  }
  
  
  @Test
  public void shouldParseListValues() {
    FeatureSetting actual = parse("+BAR(things[1] things[2] things[3] things[4] size[42])");
    assertThat(actual.getList("things")).contains("1","2","3","4");
  }
  
  
  private FeatureSetting parse(String dsl) {
    List<FeatureSetting> actual = testee.parseFeatures(Collections.singletonList(dsl));
    return actual.get(0);
  }

}
