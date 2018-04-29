package com.example.java8;

import org.junit.Test;

import com.example.java8.AnonymousClass;

import static org.junit.Assert.assertEquals;

/**
 * @author iirekm@gmail.com
 */
public class AnonymousClassTest {
    @Test
    public void works() {
        assertEquals(3, new AnonymousClass().foo());
    }
}
