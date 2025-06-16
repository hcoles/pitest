package org.pitest.coverage;

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
}
