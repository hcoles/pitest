package com.example.history;

import org.pitest.simpletest.TestAnnotationForTesting;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassBTest {


    @TestAnnotationForTesting
    public void testClassA() {
        assertThat(new ClassB().returnOne()).isEqualTo(1);
    }

}
