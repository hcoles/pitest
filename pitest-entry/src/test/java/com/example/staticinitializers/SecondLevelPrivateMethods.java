package com.example.staticinitializers;

public class SecondLevelPrivateMethods {
    static {
        dontMutate1();
    }

    private static void dontMutate1() {
        dontMutate2();
        System.out.println("mutate me");
    }

    private static void dontMutate2() {
        System.out.println("mutate me");
    }
}
