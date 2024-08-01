package com.example.staticinitializers.delayedexecution;

import java.util.function.Supplier;

public class StaticSupplierField {
    final static Supplier<String> SUPPLER = canMutate();

    private static Supplier<String> canMutate() {
        // don't mutate
        System.out.println("ideally would mutate me");

        return () -> "Do not mutate"; // mutate
    }
}
