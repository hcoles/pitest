package org.pitest.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.util.Optional;

public class FeatureSettingTest {

  FeatureSetting testee;
  private final Map<String, List<String>> values = new HashMap<>();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void shouldReturnNoneWhenNoneSupplied() {
    this.testee = new FeatureSetting("name", ToggleStatus.ACTIVATE,  this.values);
    assertThat(this.testee.getString("foo")).isEqualTo(Optional.empty());
  }


  @Test
  public void shouldReturnSingleValuesWhenSupplied() {
    this.values.put("foo", Arrays.asList("1"));
    this.testee = new FeatureSetting("name", ToggleStatus.ACTIVATE,  this.values);
    assertThat(this.testee.getString("foo")).isEqualTo(Optional.ofNullable("1"));
  }

  @Test
  public void shouldThrowErrorWhenMultipleItemsSuppliedForNonList() {
    this.values.put("foo", Arrays.asList("1", "2"));
    this.testee = new FeatureSetting("name", ToggleStatus.ACTIVATE,  this.values);

    this.thrown.expect(IllegalArgumentException.class);
    this.testee.getString("foo");
  }

  @Test
  public void shouldReturnMultipleValuesForLists() {
    this.values.put("foo", Arrays.asList("1", "2", "3"));
    this.testee = new FeatureSetting("name", ToggleStatus.ACTIVATE,  this.values);
    assertThat(this.testee.getList("foo")).contains("1", "2", "3");
  }

}
