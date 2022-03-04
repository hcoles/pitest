package org.pitest.plugin.available;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class MissingGitFeatureTest {
    MissingGitFeature underTest = new MissingGitFeature();

    @Test
    public void providesKotlinFeature() {
        assertThat(underTest.provides().name()).isEqualTo("git");
    }

    @Test
    public void featureIsMarkedAsMissing() {
        assertThat(underTest.provides().isMissing()).isTrue();
    }

    @Test
    public void throwsErrorWhenRun() {
        assertThatCode(() -> underTest.createInterceptor(null))
                .hasMessageContaining("Git integration requires the Git plugin to be installed");
    }
}