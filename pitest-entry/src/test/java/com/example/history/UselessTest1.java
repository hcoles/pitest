package com.example.history;

import org.pitest.simpletest.TestAnnotationForTesting;

public class UselessTest1 {
    @TestAnnotationForTesting
    public void fastTest() {
        new ClassA().returnOne();
    }

    @TestAnnotationForTesting
    public void fastTest2() {
        new ClassA().returnOne();
    }
}
