package com.example.matching;

public class ExhaustiveSwitch {
    private static String render(AnEnum e) {
        return switch (e) {
            case ONE -> "a";
            case TWO -> "b";
            case THREE -> "c";
            case FOUR -> "d";
        };
    }
}
