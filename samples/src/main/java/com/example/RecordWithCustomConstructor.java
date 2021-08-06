package com.example;

public record RecordWithCustomConstructor(Long timeStamp, String data) {
    public RecordWithCustomConstructor() {
        this(1L, "");
        System.out.println("mutate me");
    }
}