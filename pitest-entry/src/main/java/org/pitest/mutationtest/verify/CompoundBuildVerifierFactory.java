package org.pitest.mutationtest.verify;

import java.util.List;
import java.util.stream.Collectors;

public class CompoundBuildVerifierFactory implements BuildVerifierFactory {

    List<BuildVerifierFactory> verifiers;

    public CompoundBuildVerifierFactory(List<BuildVerifierFactory> verifiers) {
        this.verifiers = verifiers;
    }

    @Override
    public BuildVerifier create(BuildVerifierArguments args) {
        List<BuildMessage> issues = verifiers.stream()
                .map(f -> f.create(args))
                .flatMap(v -> v.verifyBuild().stream())
                .collect(Collectors.toList());

        return new BuildVerifier() {
            @Override
            public List<BuildMessage> verifyBuild() {
                return issues;
            }
        };
    }

    @Override
    public String description() {
        return "Build Verifier";
    }
}
