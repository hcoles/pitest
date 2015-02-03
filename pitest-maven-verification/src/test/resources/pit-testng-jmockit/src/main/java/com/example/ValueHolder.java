package com.example;

public class ValueHolder {

    private final int value;

    public ValueHolder( int value ) {
        if ( value < 0 ) {
            throw new IllegalArgumentException();
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
