package org.pitest.plugin;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FeatureSelectorTest {

  FeatureSelector<AFeature> testee;

  ProvidesInternalFeature internalFeature = new ProvidesInternalFeature();
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
    final FeatureSetting enableBar = new FeatureSetting("bar", ToggleStatus.ACTIVATE, new HashMap<>());
    this.testee = new FeatureSelector<>(Arrays.asList(enableBar), features(this.onByDefault, this.offByDefault));

    assertThat(this.testee.getActiveFeatures()).containsOnly(this.offByDefault, this.onByDefault);
  }

  @Test
  public void shouldDisableFeaturesWhenRequested() {
    final FeatureSetting disableFoo = new FeatureSetting("foo", ToggleStatus.DEACTIVATE, new HashMap<>());
    this.testee = new FeatureSelector<>(Arrays.asList(disableFoo), features(this.onByDefault));

    assertThat(this.testee.getActiveFeatures()).isEmpty();
  }

  @Test
  public void shouldNoDisableInternalFeatures() {
    final FeatureSetting disableFoo = new FeatureSetting("foo", ToggleStatus.DEACTIVATE, new HashMap<>());
    this.testee = new FeatureSelector<>(Arrays.asList(disableFoo), features(this.internalFeature));

    assertThat(this.testee.getActiveFeatures()).isNotEmpty();
  }

  @Test
  public void shouldProvideConfigurationForFeatureWhenProvided() {
    final FeatureSetting fooConfig = new FeatureSetting("foo", ToggleStatus.DEACTIVATE,  new HashMap<>());
    this.testee = new FeatureSelector<>(Arrays.asList(fooConfig), features(this.onByDefault));

    assertThat(this.testee.getSettingForFeature("foo")).isEqualTo(fooConfig);
    assertThat(this.testee.getSettingForFeature("bar")).isNull();
  }

  @Test
  public void featureNamesAreCaseInsensitive() {
    final FeatureSetting fooConfig = new FeatureSetting("foo", ToggleStatus.DEACTIVATE,  new HashMap<>());
    this.testee = new FeatureSelector<>(Arrays.asList(fooConfig), features(this.onByDefault));

    assertThat(this.testee.getSettingForFeature("FOO")).isEqualTo(fooConfig);
  }

  @Test
  public void doesNotDuplicateFeatures() {
    final FeatureSetting fooConfig = new FeatureSetting("foo", ToggleStatus.ACTIVATE,  new HashMap<>());
    this.testee = new FeatureSelector<>(Arrays.asList(fooConfig), features(this.onByDefault));

    assertThat(this.testee.getActiveFeatures()).hasSize(1);
  }

  @Test
  public void ordersFeaturesConsistently() {
    final FeatureSetting fooConfig = new FeatureSetting("bar", ToggleStatus.ACTIVATE,  new HashMap<>());
    this.testee = new FeatureSelector<>(Arrays.asList(fooConfig), features(this.onByDefault, this.offByDefault));

    assertThat(this.testee.getActiveFeatures()).containsExactly(offByDefault, onByDefault);

    // swap order
    this.testee = new FeatureSelector<>(Arrays.asList(fooConfig), features(this.offByDefault, this.onByDefault));

    assertThat(this.testee.getActiveFeatures()).containsExactly(offByDefault, onByDefault);

  }

  @Test
  public void ordersLowerOrderValueFeaturesFirst() {
    ProvidesFooByDefaultWithOrder1 order1 = new ProvidesFooByDefaultWithOrder1();
    final FeatureSetting fooConfig = new FeatureSetting("bar", ToggleStatus.ACTIVATE,  new HashMap<>());
    this.testee = new FeatureSelector<>(Arrays.asList(fooConfig), features(order1, this.offByDefault));

    assertThat(this.testee.getActiveFeatures()).containsExactly(order1, offByDefault);

    // swap order
    this.testee = new FeatureSelector<>(Arrays.asList(fooConfig), features(this.offByDefault, order1));

    assertThat(this.testee.getActiveFeatures()).containsExactly(order1, offByDefault);

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

class ProvidesFooByDefaultWithOrder1 implements AFeature {

  @Override
  public Feature provides() {
    return Feature.named("foo")
            .withOnByDefault(true)
            .withOrder(1);
  }

}

class ProvidesBarOptionally implements AFeature {

  @Override
  public Feature provides() {
    return Feature.named("bar").withOnByDefault(false);
  }

}

class ProvidesInternalFeature implements AFeature {

  @Override
  public Feature provides() {
    return Feature.named("bar").withOnByDefault(true).asInternalFeature();
  }

}
