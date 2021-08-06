package com.example;

import java.util.List;

public record RecordWithCustomEquals(List<Integer> ints, String data) {
    public boolean equals(Object o) {
        return o == this;
    }
}
