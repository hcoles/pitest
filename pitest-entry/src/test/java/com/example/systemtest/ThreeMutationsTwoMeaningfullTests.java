package com.example.systemtest;

import org.pitest.simpletest.TestAnnotationForTesting;

import static org.junit.Assert.assertEquals;

public class ThreeMutationsTwoMeaningfullTests {
    @TestAnnotationForTesting
    public void testReturnOne() {
        assertEquals(1, ThreeMutations.returnOne());
    }

    @TestAnnotationForTesting
    public void testReturnTwo() {
        assertEquals(2, ThreeMutations.returnTwo());
    }

    @TestAnnotationForTesting
    public void coverButDoNotTestReturnThree() {
        ThreeMutations.returnThree();
    }
}
