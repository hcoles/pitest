package com.example.coverage.execute.samples.executionindiscovery;

import org.pitest.executingtest.ExecutingTest;

import static org.assertj.core.api.Assertions.assertThat;

public class AnExecutingTest {
    @ExecutingTest
    public void aTest() {
        ATesteeClass a = new ATesteeClass();
        assertThat(a.foo(1)).isEqualTo("bizz");
    }
}
