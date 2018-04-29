package org.pitest.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FeatureSelectorTest {

  FeatureSelector<AFeature> testee;

  ProvidesFooByDefault onByDefault = new ProvidesFooByDefault();
  ProvidesBarOptionally offByDefault = new ProvidesBarOptionally();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void shouldSelectFeaturesThatAreOnByDefault() {
    final ProvidesFooByDefault onByDefault = new ProvidesFooByDefault();
    this.testee = new FeatureSelector<>(noSettings(), features(onByDefault));

    assertThat(this.testee.getActiveFeatures()).contains(onByDefault);
  }

  @Test
  public void shouldSelectFeaturesThatAreOffByDefault() {
    this.testee = new FeatureSelector<>(noSettings(), features(this.onByDefault, this.offByDefault));

    assertThat(this.testee.getActiveFeatures()).containsOnly(this.onByDefault);
  }

  @Test
  public void shouldEnableFeaturesWhenRequested() {
    final FeatureSetting enableBar = new FeatureSetting("bar", ToggleStatus.ACTIVATE,  new HashMap<String, List<String>>());
    this.testee = new FeatureSelector<>(Arrays.asList(enableBar), features(this.onByDefault, this.offByDefault));

    assertThat(this.testee.getActiveFeatures()).containsOnly(this.offByDefault, this.onByDefault);
  }

  @Test
  public void shouldDisableFeaturesWhenRequested() {
    final FeatureSetting disableFoo = new FeatureSetting("foo", ToggleStatus.DEACTIVATE,  new HashMap<String, List<String>>());
    this.testee = new FeatureSelector<>(Arrays.asList(disableFoo), features(this.onByDefault));

    assertThat(this.testee.getActiveFeatures()).isEmpty();
  }

  @Test
  public void shouldThrowErrorWhenConfigForUnknownFeatureProvided() {
    final FeatureSetting wrong = new FeatureSetting("unknown", ToggleStatus.DEACTIVATE,  new HashMap<String, List<String>>());

    this.thrown.expect(IllegalArgumentException.class);
    this.testee = new FeatureSelector<>(Arrays.asList(wrong), features(this.onByDefault));
  }

  @Test
  public void shouldProvideConfigurationForFeatureWhenProvided() {
    final FeatureSetting fooConfig = new FeatureSetting("foo", ToggleStatus.DEACTIVATE,  new HashMap<String, List<String>>());
    this.testee = new FeatureSelector<>(Arrays.asList(fooConfig), features(this.onByDefault));

    assertThat(this.testee.getSettingForFeature("foo")).isEqualTo(fooConfig);
    assertThat(this.testee.getSettingForFeature("bar")).isNull();
  }

  private List<FeatureSetting> noSettings() {
    return Collections.emptyList();
  }

  private List<AFeature> features(AFeature ...features) {
    return Arrays.asList(features);
  }
}

interface AFeature extends ProvidesFeature {

}

class ProvidesFooByDefault implements AFeature {

  @Override
  public Feature provides() {
    return Feature.named("foo").withOnByDefault(true);
  }

}

class ProvidesBarOptionally implements AFeature {

  @Override
  public Feature provides() {
    return Feature.named("bar").withOnByDefault(false);
  }

}
