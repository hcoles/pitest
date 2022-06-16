package com.example.systemtest;

import java.util.ArrayList;
import java.util.List;

public class EatsMemoryWhenMutated {
    public static int loop() throws InterruptedException {
        int i = 1;
        final List<String[]> vals = new ArrayList<>();
        Thread.sleep(1500);
        do {
            i++;
            vals.add(new String[9999999]);
            vals.add(new String[9999999]);
            vals.add(new String[9999999]);
            vals.add(new String[9999999]);
        } while (i < 1);
        i++;
        return i;
    }
}
