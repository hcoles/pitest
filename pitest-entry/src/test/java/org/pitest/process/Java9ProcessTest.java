package org.pitest.process;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Java9ProcessTest {

    @Test
    public void escapesJavaAgentLinesWithSpaces() {
        String actual = Java9Process.escapeSpaces("-javaagent:foo bar");
        assertThat(actual).isEqualTo("-javaagent:\"foo bar\"");
    }

    @Test
    public void doesntTouchAgentsWithNoSpaces() {
        String actual = Java9Process.escapeSpaces("-javaagent:foobar");
        assertThat(actual).isEqualTo("-javaagent:foobar");
    }

    @Test
    public void escapesVariableWithSpaces() {
        String actual = Java9Process.escapeSpaces("-Dthingy=foo bar");
        assertThat(actual).isEqualTo("-Dthingy=\"foo bar\"");
    }

    @Test
    public void doesntTouchVariablesWithoutSpaces() {
        String actual = Java9Process.escapeSpaces("-Dthingy=foobar");
        assertThat(actual).isEqualTo("-Dthingy=foobar");
    }
}