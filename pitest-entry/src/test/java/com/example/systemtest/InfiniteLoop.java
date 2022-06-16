package com.example.systemtest;

public class InfiniteLoop {
    public static int loop() {
        int i = 1;
        do {
            i++;
            try {
                Thread.sleep(1);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        } while (i < 1);
        i++;
        return i;
    }
}
