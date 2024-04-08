package org.pitest.mutationtest.autoconfig;

import org.pitest.mutationtest.config.ConfigurationUpdater;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureSetting;

import static java.util.Collections.singletonList;

/**
 * Gradle and maven both now automatically set the -ea flag to
 * enable assertions when running tests. Pitest must therefore
 * do it too.
 */
public class EnableAssertions implements ConfigurationUpdater {

    @Override
    public void updateConfig(FeatureSetting conf, ReportOptions toModify) {
        toModify.addChildJVMArgs(singletonList("-ea"));
    }

    @Override
    public Feature provides() {
        return Feature.named("AUTO_ASSERTIONS")
                .withOnByDefault(true)
                .withDescription(description());
    }

    @Override
    public String description() {
        return "Automatically add -ea to launch args to enable assertions";
    }

}