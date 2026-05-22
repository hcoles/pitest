package org.pitest.mutationtest.report.progress;

import org.junit.Test;
import org.pitest.mutationtest.ListenerArguments;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.plugin.FeatureSetting;
import org.pitest.plugin.ProvidesFeature;
import org.pitest.plugin.ToggleStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ProgressListenerFactoryTest {

    ProgressListenerFactory underTest = new ProgressListenerFactory();

    @Test
    public void isOnSpiChain() {
        assertThat(PluginServices.makeForContextLoader().findFeatures().stream()
                .map(ProvidesFeature::getClass)
                .collect(Collectors.toList()))
                .contains(ProgressListenerFactory.class);
    }

    @Test
    public void providesProgressFeature() {
        assertThat(underTest.provides().name()).isEqualTo("progress");
        assertThat(underTest.provides().isOnByDefault()).isFalse();
    }

    @Test
    public void createsListenerWithDefaultInterval() {
        FeatureSetting setting = new FeatureSetting("progress", ToggleStatus.ACTIVATE, Collections.emptyMap());
        ListenerArguments args = listenerArgsWithSetting(setting);
        ProgressListener listener = (ProgressListener) underTest.getListener(null, args);
        assertThat(listener.intervalSeconds()).isEqualTo(30);
    }

    @Test
    public void createsListenerWithConfiguredInterval() {
        Map<String, List<String>> settings = new HashMap<>();
        settings.put("interval", Collections.singletonList("60"));
        FeatureSetting setting = new FeatureSetting("progress", ToggleStatus.ACTIVATE, settings);
        ListenerArguments args = listenerArgsWithSetting(setting);
        ProgressListener listener = (ProgressListener) underTest.getListener(null, args);
        assertThat(listener.intervalSeconds()).isEqualTo(60);
    }

    private ListenerArguments listenerArgsWithSetting(FeatureSetting setting) {
        return new ListenerArguments(null, null, null, null, 0, false, null, Collections.emptyList())
                .withSetting(setting);
    }
}
