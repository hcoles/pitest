package com.example.java8;

/**
 * @author iirekm@gmail.com
 */
public class AnonymousClass {
    public int foo() {
        final int[] result = new int[1];
        Runnable r = new Runnable() {
            @Override
            public void run() {
                int i = 1;
                i++;
                i++;
                result[0] = i;
            }
        };
        r.run();
        return result[0];
    }
}
