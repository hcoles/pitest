package com.example.staticinitializers;

public class SingletonWithWorkInInitializer {
    int num = 6;

    private static final SingletonWithWorkInInitializer INSTANCE = new SingletonWithWorkInInitializer();

    private SingletonWithWorkInInitializer() {
        doNotMutateMethodCalledFromConstructor();
    }

    public static SingletonWithWorkInInitializer getInstance() {
        return INSTANCE;
    }

    public boolean isMember6() {
        mutateMeCalledFromPublicMethod();
        return 6 == num;
    }

    private void doNotMutateMethodCalledFromConstructor() {
        System.out.println("do not mutate");
    }

    private void mutateMeCalledFromPublicMethod() {
        System.out.println("do not mutate");
    }
}