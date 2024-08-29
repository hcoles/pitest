package com.example.staticinitializers.delayedexecution;

import java.util.function.Function;
import java.util.List;

import static java.util.Arrays.asList;

public class StaticListOfFunctions {
    public static final List<Function<Integer, Integer>> FUNCTIONS =
            asList(StaticListOfFunctions::canMutate, StaticListOfFunctions::canAlsoMutate);

    private static Integer canMutate(Integer a) {
        return a + 1;
    }

    private static Integer canAlsoMutate(Integer a) {
        return a + 2;
    }
}
