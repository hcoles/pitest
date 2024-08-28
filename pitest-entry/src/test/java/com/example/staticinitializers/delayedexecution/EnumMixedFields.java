package com.example.staticinitializers.delayedexecution;

import java.util.function.Supplier;

public enum EnumMixedFields {
    A(EnumMixedFields::canMutate, doNotMutate()), B(EnumMixedFields::canMutate, doNotMutate());

    private final Supplier<String> supplier;
    private final String s;

    EnumMixedFields(Supplier<String> supplier, String s) {
        this.supplier = supplier;
        this.s = s;
    }

    private static String canMutate() {
        return "mutate me"; // mutate
    }

    private static String doNotMutate() {
        return "Do not mutate";
    }
}