package com.example.staticinitializers;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public enum EnumWithLambdaInConstructor {
    A(asList("a","b")),
    B(asList("a","b", "c"));

    private String s;

    EnumWithLambdaInConstructor(List<String> ss) {
        this.s = ss.stream()
                .peek(this::doStuff)
                .collect(Collectors.joining());
    }

    private void doStuff(String s) {
        System.out.println(s);
    }

}
