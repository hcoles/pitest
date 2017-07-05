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
    ProvidesFooByDefault onByDefault = new ProvidesFooByDefault();
    testee = new FeatureSelector<AFeature>(noSettings(), features(onByDefault));
    
    assertThat(testee.getActiveFeatures()).contains(onByDefault);
  }

  @Test
  public void shouldSelectFeaturesThatAreOffByDefault() {
    testee = new FeatureSelector<AFeature>(noSettings(), features(onByDefault, offByDefault));
    
    assertThat(testee.getActiveFeatures()).containsOnly(onByDefault);
  }
  
  @Test
  public void shouldEnableFeaturesWhenRequested() {
    FeatureSetting enableBar = new FeatureSetting("bar", ToggleStatus.ACTIVATE,  new HashMap<String, List<String>>());
    testee = new FeatureSelector<AFeature>(Arrays.asList(enableBar), features(onByDefault, offByDefault));
    
    assertThat(testee.getActiveFeatures()).containsOnly(offByDefault, onByDefault);
  }
  
  @Test
  public void shouldDisableFeaturesWhenRequested() {
    FeatureSetting disableFoo = new FeatureSetting("foo", ToggleStatus.DEACTIVATE,  new HashMap<String, List<String>>());
    testee = new FeatureSelector<AFeature>(Arrays.asList(disableFoo), features(onByDefault));
    
    assertThat(testee.getActiveFeatures()).isEmpty();
  }
  
  @Test
  public void shouldThrowErrorWhenConfigForUnknownFeatureProvided() {
    FeatureSetting wrong = new FeatureSetting("unknown", ToggleStatus.DEACTIVATE,  new HashMap<String, List<String>>());
   
    thrown.expect(IllegalArgumentException.class);
    testee = new FeatureSelector<AFeature>(Arrays.asList(wrong), features(onByDefault));
  }
  
  @Test
  public void shouldProvideConfigurationForFeatureWhenProvided() {
    FeatureSetting fooConfig = new FeatureSetting("foo", ToggleStatus.DEACTIVATE,  new HashMap<String, List<String>>());
    testee = new FeatureSelector<AFeature>(Arrays.asList(fooConfig), features(onByDefault));
    
    assertThat(testee.getSettingForFeature("foo")).isEqualTo(fooConfig);
    assertThat(testee.getSettingForFeature("bar")).isNull();
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
