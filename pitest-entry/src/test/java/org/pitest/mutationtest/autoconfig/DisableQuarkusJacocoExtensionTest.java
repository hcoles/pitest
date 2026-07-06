package org.pitest.mutationtest.autoconfig;


import org.junit.Test;
import org.pitest.mutationtest.config.ReportOptions;

import static org.assertj.core.api.Assertions.assertThat;


public class DisableQuarkusJacocoExtensionTest {
    DisableQuarkusJacocoExtension underTest = new DisableQuarkusJacocoExtension();

    @Test
    public void isEnabledByDefault() {
        assertThat(underTest.provides().isOnByDefault()).isTrue();
    }

    @Test
    public void setsEnvironmentVariable() {
        ReportOptions data = new ReportOptions();
        underTest.updateConfig(null, data);
        assertThat(data.getEnvironmentVariables()).containsEntry("QUARKUS_JACOCO_ENABLED", "false");
    }
}