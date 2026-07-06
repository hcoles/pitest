package org.pitest.mutationtest.autoconfig;

import org.pitest.mutationtest.config.ConfigurationUpdater;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureSetting;

public class DisableQuarkusJacocoExtension implements ConfigurationUpdater {
    @Override
    public void updateConfig(FeatureSetting conf, ReportOptions toModify) {
        toModify.getEnvironmentVariables().put("QUARKUS_JACOCO_ENABLED", "false");
    }

    @Override
    public Feature provides() {
        return Feature.named("no_quarkus_jacoco")
                .withOnByDefault(true)
                .withDescription(description());
    }

    @Override
    public String description() {
        return "Auto disable Quarkus Jacoco extension";
    }
}
