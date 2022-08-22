package org.pitest.mutationtest.autoconfig;

import org.junit.Test;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.plugin.FeatureSetting;

import static org.assertj.core.api.Assertions.assertThat;

public class AutoSetThreadsTest {

    int reportedCores = 1;

    AutoSetThreads underTest = new AutoSetThreads() {
        @Override
        int getCores() {
            return reportedCores;
        }
    };

    @Test
    public void isDisabledByDefault() {
        assertThat(underTest.provides().isOnByDefault()).isFalse();
    }

    @Test
    public void leavesNumberOfThreadsAs1IfOnly1Available() {
        ReportOptions data = new ReportOptions();
        reportedCores = 1;
        data.setNumberOfThreads(1);
        underTest.updateConfig(unused(), data);
        assertThat(data.getNumberOfThreads()).isEqualTo(1);
    }

    @Test
    public void usesThreeThreadsWhen4CoresAvailable() {
        ReportOptions data = new ReportOptions();
        reportedCores = 4;
        data.setNumberOfThreads(1);
        underTest.updateConfig(unused(), data);
        assertThat(data.getNumberOfThreads()).isEqualTo(3);
    }

    @Test
    public void usesFiveThreadsWhen8CoresAvailable() {
        ReportOptions data = new ReportOptions();
        reportedCores = 8;
        data.setNumberOfThreads(1);
        underTest.updateConfig(unused(), data);
        assertThat(data.getNumberOfThreads()).isEqualTo(5);
    }

    @Test
    public void uses8CoresWhen12Available() {
        ReportOptions data = new ReportOptions();
        reportedCores = 12;
        data.setNumberOfThreads(1);
        underTest.updateConfig(unused(), data);
        assertThat(data.getNumberOfThreads()).isEqualTo(8);
    }


    private FeatureSetting unused() {
        return null;
    }
}