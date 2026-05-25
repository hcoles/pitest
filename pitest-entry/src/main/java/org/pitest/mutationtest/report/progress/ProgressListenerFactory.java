package org.pitest.mutationtest.report.progress;

import org.pitest.mutationtest.ListenerArguments;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureParameter;

import java.util.Properties;
import java.util.concurrent.Executors;

public class ProgressListenerFactory implements MutationResultListenerFactory {

    private static final int DEFAULT_INTERVAL = 30;

    private static final FeatureParameter INTERVAL = FeatureParameter.named("interval")
            .withDescription("Seconds between progress reports");

    @Override
    public MutationResultListener getListener(Properties props, ListenerArguments args) {
        int interval = args.settings()
                .flatMap(s -> s.getInteger(INTERVAL.name()))
                .orElse(DEFAULT_INTERVAL);
        return new ProgressListener(System.out, Executors.newSingleThreadScheduledExecutor(), interval);
    }

    @Override
    public String name() {
        return "progress";
    }

    @Override
    public String description() {
        return "Log progress during mutation analysis";
    }

    @Override
    public Feature provides() {
        return Feature.named("progress")
                .withOnByDefault(false)
                .withDescription(description())
                .withParameter(INTERVAL);
    }
}
