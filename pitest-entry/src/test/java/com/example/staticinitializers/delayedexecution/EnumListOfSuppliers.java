package com.example.staticinitializers.delayedexecution;

import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.asList;

public enum EnumListOfSuppliers {
    A(EnumListOfSuppliers::canMutate), B(EnumListOfSuppliers::canMutate);

    private final List<Supplier<String>> supplier;

    EnumListOfSuppliers(Supplier<String> supplier) {
        this.supplier = asList(supplier);
    }

    private static String canMutate() {
        return "mutate me"; // mutate
    }

}
