package org.pitest.coverage;

import org.pitest.plugin.Feature;
import org.pitest.util.ResultOutputStrategy;

public class BasicTestStatListenerFactory implements TestStatListenerFactory {
    @Override
    public TestStatListener createTestListener(ResultOutputStrategy unused) {
        return new BasicStatListener();
    }

    @Override
    public String description() {
        return "Basic test statistics";
    }

    @Override
    public Feature provides() {
        return Feature.named("BASIC_TEST_STATS")
                .withOnByDefault(true)
                .withDescription(description());
    }
}
