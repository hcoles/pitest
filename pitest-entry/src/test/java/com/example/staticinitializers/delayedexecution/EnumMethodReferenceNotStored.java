package com.example.staticinitializers.delayedexecution;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum EnumMethodReferenceNotStored {
    A(Arrays.asList(1,2,3));

    private final List<Integer> l;
    EnumMethodReferenceNotStored(List<Integer> list) {
        l = list.stream()
                .filter(this::doNotMutate)
                .collect(Collectors.toList());
    }

    private boolean doNotMutate(Integer i) {
        return i > 2;
    }
}
