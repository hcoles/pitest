package org.pitest.mutationtest.build;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classpath.CodeSource;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.plugin.FeatureSelector;
import org.pitest.plugin.FeatureSetting;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CompoundProjectMutationFilterFactory {

    private final FeatureSelector<ProjectMutationFilterFactory> features;

    public CompoundProjectMutationFilterFactory(List<FeatureSetting> features,
                                                Collection<ProjectMutationFilterFactory> factories) {
        this.features = new FeatureSelector<>(features, factories);
    }

    public CompoundProjectMutationFilter createFilter(ReportOptions data,
                                              CoverageDatabase coverage,
                                              ClassByteArraySource source,
                                              TestPrioritiser testPrioritiser,
                                              CodeSource code) {
        List<ProjectMutationFilter> filters = this.features.getActiveFeatures().stream()
                .map(f -> f.createFilter(new InterceptorParameters(
                        this.features.getSettingForFeature(f.provides().name()),
                        data, coverage, source, testPrioritiser, code)))
                .collect(Collectors.toList());
        return new CompoundProjectMutationFilter(filters);
    }
}
