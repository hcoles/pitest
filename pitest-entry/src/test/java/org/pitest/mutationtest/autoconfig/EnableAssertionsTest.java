package org.pitest.mutationtest.autoconfig;

import org.junit.Test;
import org.pitest.mutationtest.config.ReportOptions;

import static org.assertj.core.api.Assertions.assertThat;

public class EnableAssertionsTest {
    EnableAssertions underTest = new EnableAssertions();

    @Test
    public void addsEAFlag() {
        ReportOptions data = new ReportOptions();

        underTest.updateConfig(null, data);
        assertThat(data.getJvmArgs()).contains("-ea");
    }

    @Test
    public void featureIsNamedAutoAssertions() {
        assertThat(underTest.provides().name()).isEqualTo("auto_assertions");
    }

    @Test
    public void featureIsOnByDefault() {
        assertThat(underTest.provides().isOnByDefault()).isTrue();
    }
}