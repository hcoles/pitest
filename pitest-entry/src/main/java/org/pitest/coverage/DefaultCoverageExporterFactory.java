package org.pitest.coverage;

import org.pitest.coverage.export.DefaultCoverageExporter;
import org.pitest.plugin.Feature;
import org.pitest.util.ResultOutputStrategy;

public class DefaultCoverageExporterFactory implements CoverageExporterFactory {
    @Override
    public CoverageExporter create(ResultOutputStrategy source) {
        return new DefaultCoverageExporter(source);
    }

    @Override
    public Feature provides() {
        return Feature.named("defaultCoverage")
                .withDescription(description())
                .withOnByDefault(true);
    }

    @Override
    public String description() {
        return "Default coverage exporter";
    }
}
