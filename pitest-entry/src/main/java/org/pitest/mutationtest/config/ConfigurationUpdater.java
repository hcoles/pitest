package org.pitest.mutationtest.config;

import org.pitest.plugin.ProvidesFeature;
import org.pitest.plugin.ToolClasspathPlugin;

public interface ConfigurationUpdater extends ToolClasspathPlugin, ProvidesFeature {
    void updateConfig(ReportOptions toModify);
}
