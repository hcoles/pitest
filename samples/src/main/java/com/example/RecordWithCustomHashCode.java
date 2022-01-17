package com.example;

import java.util.List;

public record RecordWithCustomHashCode(List<Integer> ints, String data) {
    @Override
    public int hashCode() {
        return 42;
    }
}