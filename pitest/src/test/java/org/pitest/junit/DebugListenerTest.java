package org.pitest.junit;

import org.junit.Test;

import org.junit.runner.notification.Failure;

import static org.assertj.core.api.Assertions.assertThat;

public class DebugListenerTest {
    DebugListener testee = new DebugListener();

    @Test
    public void reportsErrorsWhenNoClassDefFound() {
        testee.testFailure(new Failure(null, new Exception("NoClassDefFoundError")));
        assertThat(testee.problems()).isPresent();
        assertThat(testee.problems().get()).contains("NoClassDefFoundError");
        assertThat(testee.problems().get()).contains("at org.pitest.junit.DebugListenerTest");
    }

    @Test
    public void reportsErrorsWhenClassNotFound() {
        testee.testFailure(new Failure(null, new Exception("ClassNotFoundException")));
        assertThat(testee.problems()).isPresent();
        assertThat(testee.problems().get()).contains("ClassNotFoundException");
        assertThat(testee.problems().get()).contains("at org.pitest.junit.DebugListenerTest");
    }

    @Test
    public void doesNotReportErrorsForOtherExceptions() {
        testee.testFailure(new Failure(null, new Exception("foo bar")));
        assertThat(testee.problems()).isEmpty();
    }

}