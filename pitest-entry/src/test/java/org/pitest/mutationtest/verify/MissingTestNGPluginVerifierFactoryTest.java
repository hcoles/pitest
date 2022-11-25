package org.pitest.mutationtest.verify;

import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.verifier.interceptors.BuildVerifierVerifier;

import static org.pitest.verifier.interceptors.BuildVerifierVerifier.codeSourceReturning;

public class MissingTestNGPluginVerifierFactoryTest {
    BuildVerifierVerifier v = BuildVerifierVerifier.confirmFactory(new MissingTestNGPluginVerifierFactory());

    @Test
    public void isOnChain() {
        v.isOnChain();
    }

    @Test
    public void doesNotDisplayMessageWhenJUnit5NotPresent() {
        v.withCodeSource(codeSourceReturning(ClassName.fromString("not.relevant.Foo")))
                .issues()
                .isEmpty();
    }

    @Test
    public void displaysWarningWhenJUnit5PresentWithoutPlugin() {
        v.withCodeSource(codeSourceReturning(ClassName.fromString("org.testng.annotations.Test")))
                .issues()
                .isNotEmpty();
    }

    @Test
    public void doesNotDisplayWarningWhenPluginPresent() {
        v.withCodeSource(codeSourceReturning(ClassName.fromString("org.testng.annotations.Test"),
                        ClassName.fromString("org.pitest.testng.TestNGPlugin")
                ))
                .issues()
                .isEmpty();
    }
}