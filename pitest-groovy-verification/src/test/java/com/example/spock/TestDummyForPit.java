package com.example.spock;

public class TestDummyForPit {

    public int return1Junit() {
        return 1;
    }

    public int return2TestNG() {
        return 2;
    }

    public int return3Spock() {
        return 3;
    }

    public int returnParametrizedSpock(int input) {
        return input;
    }

    public int returnParametrizedTestNG(int input) {
        return input;
    }

    public int returnJUnitParams(int input) {
        return input;
    }
}
