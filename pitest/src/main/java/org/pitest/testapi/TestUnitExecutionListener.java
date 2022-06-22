package org.pitest.testapi;

/**
 * JUnit 3 and 4 had a clear separation between discovering and executing tests. This
 * is no longer the case for JUnit 5. To support both models without double executing
 * junit5 tests, this interface is used to listen for execution during discovery.
 */
public interface TestUnitExecutionListener {
    void executionStarted(Description description);
    void executionFinished(Description description, boolean passed);
}
