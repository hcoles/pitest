package com.example.coverage.execute.samples.simple;

import org.junit.Test;

public class ParentChildInitializationTest {
    @Test
    public void test() {
        new TesteeChild();
    }
}

class TesteeParent {
    static TesteeChild child = new TesteeChild();
}

class TesteeChild extends TesteeParent {
    final static Object f = "hello";

    TesteeChild() {
        System.out.println(f);
    }
}
