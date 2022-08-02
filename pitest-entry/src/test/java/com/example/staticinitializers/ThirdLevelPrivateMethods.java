package com.example.staticinitializers;

public class ThirdLevelPrivateMethods {

    static {
        dontMutate1();
        dontMutate2();
    }

    private static void dontMutate1() {
        dontMutate2();
        System.out.println("mutate me");
    }

    private static void dontMutate2() {
        dontMutate3();
        System.out.println("mutate me");
    }

    private static void dontMutate3() {
        dontMutate4();
        System.out.println("mutate me");
    }

    private static void dontMutate4() {
        System.out.println("mutate me");
    }
}
