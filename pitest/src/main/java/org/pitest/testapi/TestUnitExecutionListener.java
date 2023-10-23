package org.pitest.testapi;

/**
 * JUnit 3 and 4 had a clear separation between discovering and executing tests. This
 * is no longer the case for JUnit 5. To support both models without double executing
 * junit5 tests, this interface is used to listen for execution during discovery.
 *
 * <p>At least one of the {@code executionStarted} methods needs to be overwritten
 * by subclasses as the default implementations call each other for backwards compatibility
 * and thus would cause a stack overflow error if called.
 *
 * <p>All implementations of this interface should be thread-safe, as the methods can be
 * called from various threads and also from parallel running tests, depending on the used
 * test engine.
 */
public interface TestUnitExecutionListener {
    default void executionStarted(Description description) {
        executionStarted(description, false);
    }
    default void executionStarted(Description description, boolean suppressParallelWarning) {
        executionStarted(description);
    }

    default void executionFinished(Description description, boolean passed) {
        executionFinished(description, passed, null);
    }

    void executionFinished(Description description, boolean passed, Throwable error);
}
