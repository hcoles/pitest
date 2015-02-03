package com.example;

import org.testng.annotations.Test;

public class ValueHolderBasicTest {

    @Test
    public void test() {
        ValueHolder holder = new ValueHolder( 1 );
        int actual = holder.getValue();
        assert actual == 1 : "actual value is " + actual;
    }

}
