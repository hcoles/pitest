package com.example;

import java.util.List;
import java.util.stream.Stream;

public class HasPrivateStreamMethodUsedOnlyInSingleFlatMap {
    public Stream<String> makesCall(List<String> l) {
        return l.stream()
                .flatMap(this::aStream);

    }

    private Stream<String> aStream(String l) {
        return Stream.empty();
    }
}