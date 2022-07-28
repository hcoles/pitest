package com.example.classloaders;

import java.util.function.IntSupplier;

public class MuteeInOtherClassloader implements IntSupplier {

    @Override
    public int getAsInt() {
        pointless();
        System.out.println("Can't kill me");
        return 42;
    }

    public void pointless() {
        System.out.println("Can't kill me either");
    }
}
