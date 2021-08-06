package com.example;

import java.util.List;

public record RecordWithCustomToString(List<Integer> ints, String data) {
    @Override
    public String toString() {
        return data;
    }
}