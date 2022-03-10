package org.pitest.plugin.available;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class MissingKotlinFeatureTest  {
    MissingKotlinFeature underTest = new MissingKotlinFeature();

    @Test
    public void providesKotlinFeature() {
        assertThat(underTest.provides().name()).isEqualTo("kotlin");
    }

    @Test
    public void featureIsMarkedAsMissing() {
        assertThat(underTest.provides().isMissing()).isTrue();
    }

    @Test
    public void throwsErrorWhenRun() {
        assertThatCode(() -> underTest.createInterceptor(null))
                .hasMessageContaining("Kotlin support requires the kotlin plugin to be installed");
    }
}