package org.pitest.mutationtest.build.intercept.javafeatures;

public enum AnEnum {
    AN_INSTANCE("hello");

    AnEnum(String s) {
        System.out.println(s);
    }

    public void aMethod() {
        System.out.println("dont mutate me");
    }
}
