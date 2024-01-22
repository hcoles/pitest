package com.example.staticinitializers;

import java.util.function.Supplier;

public enum EnumFieldSupplier {
    A(canMutate());

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
