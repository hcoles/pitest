package com.example.systemtest;

import org.pitest.simpletest.TestAnnotationForTesting;

import static org.junit.Assert.assertEquals;

public class FailingTest {
    @TestAnnotationForTesting
    public void fail() {
        assertEquals(1, 2);
    }
}
