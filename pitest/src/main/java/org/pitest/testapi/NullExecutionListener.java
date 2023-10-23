package org.pitest.testapi;

public class NullExecutionListener implements TestUnitExecutionListener {
    @Override
    public void executionStarted(Description description) {
        // noop
    }

    @Override
    public void executionFinished(Description description, boolean passed, Throwable error) {
        //noop
    }
}
