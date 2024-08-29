package com.example.staticinitializers.delayedexecution;

import java.util.List;

import static java.util.Arrays.asList;

public class StaticListOfUnannotatedInterfaces {
        public static final List<CustomFunctionNotAnnotated<Integer, Integer>> FUNCTIONS =
                asList(StaticListOfUnannotatedInterfaces::canMutate, StaticListOfUnannotatedInterfaces::canAlsoMutate);

        private static Integer canMutate(Integer a) {
            return a + 1;
        }

        private static Integer canAlsoMutate(Integer a) {
            return a + 2;
        }
    }