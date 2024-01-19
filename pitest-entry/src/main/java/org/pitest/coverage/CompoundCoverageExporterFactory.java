package org.pitest.coverage;

import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureSelector;
import org.pitest.plugin.FeatureSetting;
import org.pitest.util.ResultOutputStrategy;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CompoundCoverageExporterFactory implements CoverageExporterFactory {

    private final FeatureSelector<CoverageExporterFactory> features;

    public CompoundCoverageExporterFactory(List<FeatureSetting> features, Collection<CoverageExporterFactory> children) {
        this.features = new FeatureSelector<>(features, children);
    }

    @Override
    public CoverageExporter create(ResultOutputStrategy source) {
        List<CoverageExporter> exporters = this.features.getActiveFeatures().stream()
                .map(f -> f.create(source))
                .collect(Collectors.toList());
        return c -> exporters.stream().forEach(exporter -> exporter.recordCoverage(c));
    }

    @Override
    public String description() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Feature provides() {
        throw new UnsupportedOperationException();
    }

}