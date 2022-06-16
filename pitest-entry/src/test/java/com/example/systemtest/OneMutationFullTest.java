package com.example.systemtest;

import org.pitest.simpletest.TestAnnotationForTesting;

import static org.junit.Assert.assertEquals;

public class OneMutationFullTest {
    @TestAnnotationForTesting
    public void testReturnOne() {
        assertEquals(1, OneMutationOnly.returnOne());
    }
}
