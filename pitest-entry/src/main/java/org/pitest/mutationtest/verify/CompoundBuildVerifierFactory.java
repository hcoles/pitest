package org.pitest.mutationtest.verify;

import org.pitest.classpath.CodeSource;

import java.util.List;
import java.util.stream.Collectors;

public class CompoundBuildVerifierFactory implements BuildVerifierFactory {

    List<BuildVerifierFactory> verifiers;

    public CompoundBuildVerifierFactory(List<BuildVerifierFactory> verifiers) {
        this.verifiers = verifiers;
    }

    @Override
    public BuildVerifier create(CodeSource code) {
        List<BuildIssue> issues = verifiers.stream()
                .map(f -> f.create(code))
                .flatMap(v -> v.verifyBuild().stream())
                .collect(Collectors.toList());

        return new BuildVerifier() {
            @Override
            public List<BuildIssue> verifyBuild() {
                return issues;
            }
        };
    }

    @Override
    public String description() {
        return "Build Verifier";
    }
}
