package org.pitest.mutationtest.config;

import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureSelector;
import org.pitest.plugin.FeatureSetting;

import java.util.Collection;
import java.util.List;

public class CompoundConfigurationUpdater implements ConfigurationUpdater {
    private final FeatureSelector<ConfigurationUpdater> features;

    public CompoundConfigurationUpdater(List<FeatureSetting> features,
                                        Collection<ConfigurationUpdater> children) {
        this.features = new FeatureSelector<>(features, children);
    }

    @Override
    public void updateConfig(FeatureSetting unused, ReportOptions toModify) {
        for (ConfigurationUpdater each : features.getActiveFeatures() ) {
            each.updateConfig(features.getSettingForFeature(each.provides().name()), toModify);
        }
    }

    @Override
    public Feature provides() {
        return Feature.named("n/a");
    }

    @Override
    public String description() {
        return "n/a";
    }
}
