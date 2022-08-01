package com.example.staticinitializers;

public class BrokenChain {
    static {
        dontMutate1();
    }

    private static void dontMutate1() {
        dontMutate2();
        System.out.println("mutate me");
    }

    private static void dontMutate2() {
        mutateMe();
        System.out.println("mutate me");
    }

    // chain broken here by public method
    public static void mutateMe() {
        mutateMe2();
        System.out.println("mutate me");
    }

    private static void mutateMe2() {
        System.out.println("mutate me");
    }
}
