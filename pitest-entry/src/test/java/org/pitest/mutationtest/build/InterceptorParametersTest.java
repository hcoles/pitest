package org.pitest.mutationtest.build;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import java.util.Optional;
import org.pitest.plugin.FeatureParameter;
import org.pitest.plugin.FeatureSetting;

public class InterceptorParametersTest {

  InterceptorParameters testee;

  @Test
  public void shouldReturnStringParamsWhenPresent() {
    this.testee = makeFor("foo", "bar");
    assertThat(this.testee.getString(FeatureParameter.named("foo"))).isEqualTo(Optional.ofNullable("bar"));
  }

  @Test
  public void shouldReturnNoneWhenValueAbsent() {
    this.testee = makeFor("nomatch", "bar");
    assertThat(this.testee.getString(FeatureParameter.named("foo"))).isEqualTo(Optional.empty());
  }

  @Test
  public void shouldReturnNoneWhenFeatureSettingsAbsent() {
    this.testee = new InterceptorParameters(null, null, null);
    assertThat(this.testee.getString(FeatureParameter.named("foo"))).isEqualTo(Optional.empty());
  }

  @Test
  public void shouldReturnIntegerWhenPresent() {
    this.testee = makeFor("foo", "11");
    assertThat(this.testee.getInteger(FeatureParameter.named("foo"))).isEqualTo(Optional.ofNullable(11));
  }

  @Test
  public void shouldReturnListsOfStringsWhenPresent() {
    this.testee = makeFor("foo", "bar", "car");
    assertThat(this.testee.getList(FeatureParameter.named("foo"))).contains("bar", "car");
  }

  private InterceptorParameters makeFor(String key, String ... vals) {
    final Map<String, List<String>> values = new HashMap<>();
    values.put(key, Arrays.asList(vals));
    final FeatureSetting fs = new FeatureSetting(null, null,values);
    return new InterceptorParameters(fs, null, null);
  }

}
