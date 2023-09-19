package com.example.history;

import org.pitest.simpletest.TestAnnotationForTesting;

import static org.assertj.core.api.Assertions.assertThat;

public class SlowKillingTest {
    @TestAnnotationForTesting
    public void slowTest() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertThat(new ClassA().returnOne()).isEqualTo(1);
    }
}
