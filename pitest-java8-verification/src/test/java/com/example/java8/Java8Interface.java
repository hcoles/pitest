package com.example.java8;

/**
 * @author iirekm@gmail.com
 */
public interface Java8Interface {
    default int foo() {
        int i = 1;
        i++;
        i++;
        return i;
    }
}
