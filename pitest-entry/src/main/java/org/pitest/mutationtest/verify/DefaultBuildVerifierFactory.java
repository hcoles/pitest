package org.pitest.mutationtest.verify;

import org.pitest.classpath.CodeSource;

public class DefaultBuildVerifierFactory implements BuildVerifierFactory {

    @Override
    public BuildVerifier create(CodeSource code) {
        return new DefaultBuildVerifier(code);
    }

    @Override
    public String description() {
        return "Default build verifier";
    }
}
