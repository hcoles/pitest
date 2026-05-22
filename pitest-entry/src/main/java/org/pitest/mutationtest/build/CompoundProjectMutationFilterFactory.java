package org.pitest.mutationtest.build;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classpath.CodeSource;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.mutationtest.History;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.plugin.FeatureSelector;
import org.pitest.plugin.FeatureSetting;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CompoundProjectMutationFilterFactory {

    private final FeatureSelector<ProjectMutationInterceptorFactory> features;

    public CompoundProjectMutationFilterFactory(List<FeatureSetting> features,
                                                Collection<ProjectMutationInterceptorFactory> factories) {
        this.features = new FeatureSelector<>(features, factories);
    }

    public CompoundProjectMutationInterceptor createFilter(ReportOptions data,
                                                           CoverageDatabase coverage,
                                                           ClassByteArraySource source,
                                                           TestPrioritiser testPrioritiser,
                                                           CodeSource code,
                                                           History history) {
        List<ProjectMutationInterceptor> filters = this.features.getActiveFeatures().stream()
                .map(f -> f.createInterceptor(new InterceptorParameters(
                        this.features.getSettingForFeature(f.provides().name()),
                        data, coverage, source, testPrioritiser, code, history)))
                .collect(Collectors.toList());
        return new CompoundProjectMutationInterceptor(filters);
    }
}
