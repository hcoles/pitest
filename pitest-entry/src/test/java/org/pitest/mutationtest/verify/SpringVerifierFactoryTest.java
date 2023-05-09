package org.pitest.mutationtest.verify;

import org.junit.Test;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassName;
import org.pitest.verifier.interceptors.BuildVerifierVerifier;

import static org.pitest.verifier.interceptors.BuildVerifierVerifier.aValidClass;
import static org.pitest.verifier.interceptors.BuildVerifierVerifier.codeSourceForClasses;
import static org.pitest.verifier.interceptors.BuildVerifierVerifier.codeSourceReturning;

public class SpringVerifierFactoryTest {
    BuildVerifierVerifier v = BuildVerifierVerifier.confirmFactory(new SpringVerifierFactory());

    @Test
    public void isOnChain() {
        v.isOnChain();
    }

    @Test
    public void doesNotDisplayMessageWhenSpringNotPresent() {
        v.withCodeSource(codeSourceReturning(ClassName.fromString("not.relevant.Foo")))
                .issues()
                .isEmpty();
    }

    @Test
    public void displaysWarningWhenSpringPresentWithoutPlugin() {
        ClassTree springMarker = aValidClass();
        springMarker.rawNode().name = "org.springframework.core.SpringVersion";


        ClassTree clientCode = aValidClass();
        clientCode.rawNode().name = "com.example.Foo";
        clientCode.rawNode().sourceFile = "Foo.kt";

        v.withCodeSource(codeSourceForClasses(springMarker, clientCode))
                .issues()
                .isNotEmpty();
    }

    @Test
    public void doesNotDisplayWarningWhenPluginPresent() {
        ClassTree springMarker = aValidClass();
        springMarker.rawNode().name = "org.springframework.core.SpringVersion";

        ClassTree pluginMarker = aValidClass();
        pluginMarker.rawNode().name = "com.groupcdg.arcmutate.spring.PluginMarker";

        ClassTree clientCode = aValidClass();
        clientCode.rawNode().name = "com.example.Foo";
        clientCode.rawNode().sourceFile = "Foo.kt";

        v.withCodeSource(codeSourceForClasses(springMarker, pluginMarker, clientCode))
                .issues()
                .isEmpty();
    }
}

