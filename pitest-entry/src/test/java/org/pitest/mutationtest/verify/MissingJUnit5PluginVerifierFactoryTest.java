package org.pitest.mutationtest.verify;

import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.verifier.interceptors.BuildVerifierVerifier;

import static org.pitest.verifier.interceptors.BuildVerifierVerifier.codeSourceReturning;

public class MissingJUnit5PluginVerifierFactoryTest {

    BuildVerifierVerifier v = BuildVerifierVerifier.confirmFactory(new MissingJUnit5PluginVerifierFactory());

    @Test
    public void isOnChain() {
        v.isOnChain();
    }

    @Test
    public void doesNotDisplayMessageWhenJUnit5NotPresent() {
        v.withCodeSource(codeSourceReturning(ClassName.fromString("not.relevant.Foo")))
                .messages()
                .isEmpty();
    }

    @Test
    public void displaysWarningWhenJUnit5PresentWithoutPlugin() {
        v.withCodeSource(codeSourceReturning(ClassName.fromString("org.junit.jupiter.api.Test")))
                .messages()
                .isNotEmpty();
    }

    @Test
    public void doesNotDisplayWarningWhenPluginPresent() {
        v.withCodeSource(codeSourceReturning(ClassName.fromString("org.junit.jupiter.api.Test"),
                        ClassName.fromString("org.pitest.junit5.JUnit5TestPluginFactory")
                        ))
                .messages()
                .isEmpty();
    }

}