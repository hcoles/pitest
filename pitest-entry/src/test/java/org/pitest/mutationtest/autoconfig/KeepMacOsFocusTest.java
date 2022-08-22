package org.pitest.mutationtest.autoconfig;

import org.junit.Test;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.plugin.FeatureSetting;

import static org.assertj.core.api.Assertions.assertThat;

public class KeepMacOsFocusTest {
    KeepMacOsFocus underTest = new KeepMacOsFocus();

    @Test
    public void addsHeadlessTrueToJvmArgs() {
        ReportOptions data = new ReportOptions();

        underTest.updateConfig(null, data);
        assertThat(data.getJvmArgs()).contains("-Djava.awt.headless=true");
    }

    @Test
    public void featureIsNamedMacOsFocus() {
        KeepMacOsFocus underTest = new KeepMacOsFocus();

        assertThat(underTest.provides().name()).isEqualTo("macos_focus");
    }

    @Test
    public void featureIsOnByDefault() {
        KeepMacOsFocus underTest = new KeepMacOsFocus();

        assertThat(underTest.provides().isOnByDefault()).isTrue();
    }
}