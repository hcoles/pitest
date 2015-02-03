package com.example;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValueHolderDataTest {

    @Test( dataProvider = "data" )
    public void test( int expected ) {
        ValueHolder holder = new ValueHolder( expected );
        int actual = holder.getValue();
        assert actual == expected : "actual value is " + actual;
    }


    @DataProvider
    public static Object[][] data() {
        return new Object[][] {
                { 0 },
                { 1 },
                { 2 }
        };
    }

}
