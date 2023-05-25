package org.pitest.plugin;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import java.util.List;

public class FeatureTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(Feature.class)
            .withOnlyTheseFields("name")
            .verify();
  }

  @Test
  public void shouldUseOnlyNameForEquality() {
    assertThat(Feature.named("foo"))
    .isEqualTo(Feature.named("foo").withDescription("?").withOnByDefault(false));
  }

  @Test
  public void nameEqualityIsCaseInsensitive() {
    assertThat(Feature.named("foo"))
            .isEqualTo(Feature.named("FOO"));
  }

  @Test
  public void ordersByNameWhenOrderValueEqual() {
    Feature a = Feature.named("a");
    Feature b = Feature.named("b");
    Feature c = Feature.named("c");
    Feature d = Feature.named("d");
    List<Feature> features = asList(c, a, b, d);

    assertThat(features.stream().sorted()).containsExactly(a, b, c, d);
  }

  @Test
  public void ordersByOrderValueWhenValuesDiffer() {
    Feature a = Feature.named("a").withOrder(4);
    Feature b = Feature.named("b").withOrder(3);
    Feature c = Feature.named("c").withOrder(2);
    Feature d = Feature.named("d").withOrder(1);
    List<Feature> features = asList(c, a, b, d);

    assertThat(features.stream().sorted()).containsExactly(d, c, b, a);
  }

  @Test
  public void ordersFirstByValueThenByName() {
    Feature a = Feature.named("a").withOrder(4);
    Feature b = Feature.named("b").withOrder(4);
    Feature c = Feature.named("c").withOrder(2);
    Feature d = Feature.named("d").withOrder(2);
    List<Feature> features = asList(c, a, b, d);

    assertThat(features.stream().sorted()).containsExactly(c, d, a, b);
  }

}
