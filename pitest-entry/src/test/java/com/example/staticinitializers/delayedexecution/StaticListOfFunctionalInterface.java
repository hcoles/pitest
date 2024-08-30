package com.example.staticinitializers.delayedexecution;

import java.util.List;

import static java.util.Arrays.asList;

public class StaticListOfFunctionalInterface {
    public static final List<CustomFunction<Integer, Integer>> FUNCTIONS =
            asList(StaticListOfFunctionalInterface::canMutate, StaticListOfFunctionalInterface::canAlsoMutate);

    private static Integer canMutate(Integer a) {
        return a + 1;
    }

    private static Integer canAlsoMutate(Integer a) {
        return a + 2;
    }
}

