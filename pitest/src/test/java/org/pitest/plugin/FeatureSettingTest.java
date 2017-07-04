package org.pitest.plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.pitest.functional.Option;

import static org.assertj.core.api.Assertions.assertThat;

public class FeatureSettingTest {
  
  FeatureSetting testee;
  private Map<String, List<String>> values = new HashMap<String, List<String>>();
  
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void shouldReturnNoneWhenNoneSupplied() {
    testee = new FeatureSetting("name", ToggleStatus.ACTIVATE,  values);
    assertThat(testee.getString("foo")).isEqualTo(Option.none());
  }

  
  @Test
  public void shouldReturnSingleValuesWhenSupplied() {
    values.put("foo", Arrays.asList("1"));
    testee = new FeatureSetting("name", ToggleStatus.ACTIVATE,  values);
    assertThat(testee.getString("foo")).isEqualTo(Option.some("1"));
  }
  
  @Test
  public void shouldThrowErrorWhenMultipleItemsSuppliedForNonList() {
    values.put("foo", Arrays.asList("1", "2"));
    testee = new FeatureSetting("name", ToggleStatus.ACTIVATE,  values);
   
    thrown.expect(IllegalArgumentException.class);
    testee.getString("foo");
  }
  
  @Test
  public void shouldReturnMultipleValuesForLists() {
    values.put("foo", Arrays.asList("1", "2", "3"));
    testee = new FeatureSetting("name", ToggleStatus.ACTIVATE,  values);
    assertThat(testee.getList("foo")).contains("1", "2", "3");
  }

}
