package com.example.staticinitializers.delayedexecution;

import java.util.function.Function;

public class StaticFunctionField {
    private static final Function<String,String> FOO = canMutate();

    private static Function<String, String> canMutate() {
        // don't mutate
        System.out.println("ideally would mutate me");

        return s -> s + "foo";
    }
}
