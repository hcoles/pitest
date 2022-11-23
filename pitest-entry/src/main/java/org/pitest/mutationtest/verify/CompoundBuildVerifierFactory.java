package org.pitest.mutationtest.verify;

import org.pitest.classpath.CodeSource;

import java.util.List;

public class CompoundBuildVerifierFactory implements BuildVerifierFactory {

    List<BuildVerifierFactory> verifiers;

    public CompoundBuildVerifierFactory(List<BuildVerifierFactory> verifiers) {
        this.verifiers = verifiers;
    }

    @Override
    public BuildVerifier create(CodeSource code) {
        return () -> verifiers.stream()
                .map(f -> f.create(code))
                .forEach(BuildVerifier::verify);
    }

    @Override
    public String description() {
        return "Build Verifier";
    }
}
