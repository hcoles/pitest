package org.pitest.bytecode.analysis;

import org.junit.Test;
import org.pitest.classpath.ClassloaderByteArraySource;

import static org.assertj.core.api.Assertions.assertThat;

public class MethodTreeTest {

    ClassloaderByteArraySource bytes = ClassloaderByteArraySource.fromContext();

    @Test
    public void retrievesInstructionsWithinMethod() {
        var clazz = loadClass(MethodTreeTest.class.getName());
        var method = findMethod(clazz, "retrievesInstructionsWithinMethod");
        assertThat(method.instructionForIndex(0)).isNotEmpty();
        assertThat(method.instructionForIndex(-1)).isEmpty();
        assertThat(method.instructionForIndex(Integer.MAX_VALUE)).isEmpty();
    }

    @Test
    public void recognisesAbstractMethods() {
        var clazz = loadClass(Bar.class.getName());
        var isAbstract = findMethod(clazz, "foo");
        var isConcrete = findMethod(clazz, "bar");

        assertThat(isAbstract.isAbstract()).isTrue();
        assertThat(isConcrete.isAbstract()).isFalse();
    }

    @Test
    public void recognisesAbstractMethodsInterfaces() {
        var clazz = loadClass(Foo.class.getName());
        var method = findMethod(clazz, "foo");

        assertThat(method.isAbstract()).isTrue();
    }

    @Test
    public void recognisesPrivateMethods() {
        var clazz = loadClass(Bar.class.getName());
        var isPrivate = findMethod(clazz, "cantSeeMe");
        var notPrivate = findMethod(clazz, "foo");

        assertThat(isPrivate.isPrivate()).isTrue();
        assertThat(notPrivate.isPrivate()).isFalse();
    }


    private static MethodTree findMethod(ClassTree clazz, String name) {
        var method = clazz.methods().stream()
                .filter(f -> f.rawNode().name.equals(name))
                .findFirst().get();
        return method;
    }


    private ClassTree loadClass(String Name) {
        return ClassTree.fromBytes(bytes.getBytes(Name).get());
    }
}

interface Foo {
    void foo();
}

abstract class Bar {

    public abstract void foo();

    void bar() {

    }

    private void cantSeeMe() {

    }
}