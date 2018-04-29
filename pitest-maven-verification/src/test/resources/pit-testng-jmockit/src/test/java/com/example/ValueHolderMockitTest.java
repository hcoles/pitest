package com.example;

import mockit.Expectations;
import mockit.Mocked;
import org.testng.annotations.Test;

public class ValueHolderMockitTest {

    @Test
    public void test( @Mocked final ValueHolder holder ) {
        new Expectations() {{
            holder.getValue();
            result = 1;
        }};
        int value = holder.getValue();
        assert value == 1 : "value is " + value;
    }

}
