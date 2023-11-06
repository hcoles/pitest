package com.example.staticinitializers;

import java.util.stream.Stream;

public class NestedEnumWithLambdaInStaticInitializer {
    private String name = "Toto";

    public NestedEnumWithLambdaInStaticInitializer(){}

    public String getName() {
        return name;
    }

    public enum TOYS {
        BALL("his_ball"),
        MONKEY("his_monkey");

        private static final String[] toys = Stream.of(TOYS.values()).map(TOYS::getLink).toArray(String[]::new);
        private String toy;

        TOYS(String theToy) {
            this.toy = theToy;
        }

        public String getLink() {
            return toy;
        }

    }
}