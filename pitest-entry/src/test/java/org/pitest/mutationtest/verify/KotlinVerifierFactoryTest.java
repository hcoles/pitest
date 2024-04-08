package org.pitest.mutationtest.verify;

import org.junit.Test;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassName;
import org.pitest.verifier.interceptors.BuildVerifierVerifier;

import static org.pitest.verifier.interceptors.BuildVerifierVerifier.aValidClass;
import static org.pitest.verifier.interceptors.BuildVerifierVerifier.codeSourceForClasses;
import static org.pitest.verifier.interceptors.BuildVerifierVerifier.codeSourceReturning;

public class KotlinVerifierFactoryTest {

    BuildVerifierVerifier v = BuildVerifierVerifier.confirmFactory(new KotlinVerifierFactory());

    @Test
    public void isOnChain() {
        v.isOnChain();
    }

    @Test
    public void doesNotDisplayMessageWhenKotlinNotPresent() {
        v.withCodeSource(codeSourceReturning(ClassName.fromString("not.relevant.Foo")))
                .messages()
                .isEmpty();
    }

    @Test
    public void displaysWarningWhenKotlinLibPresentAndKotlinClassesPresent() {
        ClassTree kotlinMarker = aValidClass();
        kotlinMarker.rawNode().name = "kotlin.KotlinVersion";


        ClassTree clientCode = aValidClass();
        clientCode.rawNode().name = "com.example.Foo";
        clientCode.rawNode().sourceFile = "Foo.kt";

        v.withCodeSource(codeSourceForClasses(kotlinMarker, clientCode))
                .messages()
                .isNotEmpty();
    }

    @Test
    public void doesNotDisplayWarningWhenPluginPresent() {
        ClassTree kotlinMarker = aValidClass();
        kotlinMarker.rawNode().name = "kotlin.KotlinVersion";

        ClassTree interceptor = aValidClass();
        interceptor.rawNode().name = "com.groupcdg.pitest.kotlin.KotlinFilterInterceptor";

        ClassTree clientCode = aValidClass();
        clientCode.rawNode().name = "com.example.Foo";
        clientCode.rawNode().sourceFile = "Foo.kt";

        v.withCodeSource(codeSourceForClasses(kotlinMarker, interceptor, clientCode))
                .messages()
                .isEmpty();
    }

    @Test
    public void doesNotDisplayWarningWhenNoKotlinCodePresent() {
        v.withCodeSource(codeSourceReturning(ClassName.fromString("kotlin.KotlinVersion"),
                        ClassName.fromString("com.groupcdg.pitest.kotlin.KotlinFilterInterceptor")
                ))
                .messages()
                .isEmpty();
    }

}

