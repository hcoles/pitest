package com.example.staticinitializers;

public class MethodsCallsEachOtherInLoop {
    static {
        a(true);
    }

    private static void a(boolean bool) {
        if (bool) {
            b(false);
        }
    }

    private static void b(boolean bool) {
        if (bool) {
            a(false);
        }
    }
}
