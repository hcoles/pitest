package com.example.staticinitializers.delayedexecution;

@FunctionalInterface
public interface CustomFunction <T, R> {
    R apply(T t);
}