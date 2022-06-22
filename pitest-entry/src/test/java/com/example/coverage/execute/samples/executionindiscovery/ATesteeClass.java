package com.example.coverage.execute.samples.executionindiscovery;

public class ATesteeClass {

    public String foo(int i) {
        if (i > 10) {
            return "fuzz";
        }
        return "bizz";
    }
}
