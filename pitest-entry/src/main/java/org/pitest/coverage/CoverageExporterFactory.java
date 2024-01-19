package org.pitest.coverage;

import org.pitest.plugin.ProvidesFeature;
import org.pitest.plugin.ToolClasspathPlugin;
import org.pitest.util.ResultOutputStrategy;

public interface CoverageExporterFactory extends ToolClasspathPlugin, ProvidesFeature {
    CoverageExporter create(ResultOutputStrategy source);
}
