package org.pitest.mutationtest.verify;

import org.junit.Test;
import org.pitest.verifier.interceptors.BuildVerifierVerifier;

public class DefaultBuildVerifierFactoryTest {

    BuildVerifierVerifier v = BuildVerifierVerifier.confirmFactory(new DefaultBuildVerifierFactory());

    @Test
    public void isOnChain() {
        v.isOnChain();
    }
}