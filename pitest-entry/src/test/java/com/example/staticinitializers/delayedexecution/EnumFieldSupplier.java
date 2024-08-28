package com.example.staticinitializers.delayedexecution;

import java.util.function.Supplier;

public enum EnumFieldSupplier {
    A(canMutate()), B(canMutate());

    private final Supplier<String> supplier;

    EnumFieldSupplier(Supplier<String> supplier) {
        this.supplier = supplier;
    }

    private static Supplier<String> canMutate() {
        // don't mutate
        System.out.println("ideally would mutate me");

        return () -> "Do not mutate"; // mutate
    }
}
