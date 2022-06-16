package com.example.systemtest;

import org.pitest.simpletest.TestAnnotationForTesting;

import static org.junit.Assert.assertEquals;

public class OneMutationFullTestWithSystemPropertyDependency {
    @TestAnnotationForTesting
    public void testReturnOne() {
        if (System.getProperty("foo").equals("foo")) {
            assertEquals(1, OneMutationOnly.returnOne());
        }
    }
}
