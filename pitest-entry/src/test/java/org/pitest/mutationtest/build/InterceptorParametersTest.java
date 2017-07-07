package org.pitest.mutationtest.build;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pitest.functional.Option;
import org.pitest.plugin.FeatureParameter;
import org.pitest.plugin.FeatureSetting;

public class InterceptorParametersTest {
  
  InterceptorParameters testee;
  
  @Test
  public void shouldReturnStringParamsWhenPresent() {
    testee = makeFor("foo", "bar");
    assertThat(testee.getString(FeatureParameter.named("foo"))).isEqualTo(Option.some("bar"));
  }
  
  @Test
  public void shouldReturnNoneWhenValueAbsent() {
    testee = makeFor("nomatch", "bar");
    assertThat(testee.getString(FeatureParameter.named("foo"))).isEqualTo(Option.none());
  }

  @Test
  public void shouldReturnNoneWhenFeatureSettingsAbsent() {
    testee = new InterceptorParameters(null, null, null);
    assertThat(testee.getString(FeatureParameter.named("foo"))).isEqualTo(Option.none());
  }
  
  @Test
  public void shouldReturnIntegerWhenPresent() {
    testee = makeFor("foo", "11");
    assertThat(testee.getInteger(FeatureParameter.named("foo"))).isEqualTo(Option.some(11));
  }
  
  @Test
  public void shouldReturnListsOfStringsWhenPresent() {
    testee = makeFor("foo", "bar", "car");
    assertThat(testee.getList(FeatureParameter.named("foo"))).contains("bar", "car");
  }
  
  private InterceptorParameters makeFor(String key, String ... vals) {
    Map<String, List<String>> values = new HashMap<String,List<String>>();
    values.put(key, Arrays.asList(vals));
    FeatureSetting fs = new FeatureSetting(null, null,values);
    return new InterceptorParameters(fs, null, null);
  }
  
}
