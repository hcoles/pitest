package com.example.java8;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author iirekm@gmail.com
 */
public class Java8LambdaExpressionTest {
    @Test
    public void works() {
        assertEquals(3, new Java8LambdaExpression().foo());
    }
}
