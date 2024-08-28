package com.example.staticinitializers.delayedexecution;

import java.util.function.Supplier;

public enum EnumFieldMethodRef {
    A(EnumFieldMethodRef::canMutate), B(EnumFieldMethodRef::canMutate);

    private final Supplier<String> supplier;


    EnumFieldMethodRef(Supplier<String> supplier) {
        this.supplier = supplier;
    }

    private static String canMutate() {
        return "Do not mutate";
    }
}
