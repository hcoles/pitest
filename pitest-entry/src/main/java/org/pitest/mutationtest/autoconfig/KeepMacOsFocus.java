package org.pitest.mutationtest.autoconfig;

import org.pitest.mutationtest.config.ConfigurationUpdater;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureSetting;

import static java.util.Collections.singletonList;

/**
 * Pitest steals keyboard focus in OSX unless java is launched in
 * headless mode.
 */
public class KeepMacOsFocus implements ConfigurationUpdater {

    @Override
    public void updateConfig(FeatureSetting conf, ReportOptions toModify) {
        toModify.addChildJVMArgs(singletonList("-Djava.awt.headless=true"));
    }

    @Override
    public Feature provides() {
        return Feature.named("MACOS_FOCUS")
                .withOnByDefault(true)
                .withDescription(description());
    }

    @Override
    public String description() {
        return "Auto add java.awt.headless=true to keep keyboard focus on Mac OS";
    }

}
