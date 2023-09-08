package com.example.history;

import org.pitest.simpletest.TestAnnotationForTesting;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassATest {

    @TestAnnotationForTesting
    public void testClassA() {
        assertThat(new ClassA().returnOne()).isEqualTo(1);
    }

}