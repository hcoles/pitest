package org.pitest.classpath;

import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.plugin.FeatureSelector;
import org.pitest.plugin.FeatureSetting;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CompoundSourceDecoratorFactory {
    private final FeatureSelector<CodeSourceDecoratorFactory> features;

    public CompoundSourceDecoratorFactory(List<FeatureSetting> features,
                                          Collection<CodeSourceDecoratorFactory> decorators) {
        this.features = new FeatureSelector<>(features, decorators);
    }

    public CodeSourceDecorator createDecorator(
            ReportOptions data) {
        List<CodeSourceDecorator> decorators = this.features.getActiveFeatures().stream()
                .map(f -> f.createDecorator(new CodeSourceParams(features.getSettingForFeature(f.provides().name()), data)))
                .collect(Collectors.toList());

        return c -> {
            CodeSource source = c;
            for (CodeSourceDecorator each : decorators) {
                source = each.decorate(source);
            }

            return source;
        };

    }
}