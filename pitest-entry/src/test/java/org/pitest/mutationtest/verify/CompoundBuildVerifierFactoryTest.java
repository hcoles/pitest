package org.pitest.mutationtest.verify;

import org.junit.Test;
import org.mockito.Mockito;
import org.pitest.classpath.CodeSource;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.pitest.mutationtest.verify.BuildIssue.issue;

public class CompoundBuildVerifierFactoryTest {

    @Test
    public void returnsResultsFromChildren() {
        CompoundBuildVerifierFactory underTest = new CompoundBuildVerifierFactory(asList(
                factoryFor(buildVerifier(asList("one"))),
                factoryFor(buildVerifier(asList("two", "three")))));

        assertThat(underTest.create(aCodeSource()).verifyBuild())
                .containsExactly(issue("one"), issue("two"), issue("three"));
    }

    private BuildVerifierFactory factoryFor(BuildVerifier bv) {
        BuildVerifierFactory vs = Mockito.mock(BuildVerifierFactory.class);
        when(vs.create(any(CodeSource.class))).thenReturn(bv);
        return vs;
    }

    private CodeSource aCodeSource() {
        return Mockito.mock(CodeSource.class);
    }

    private BuildVerifier buildVerifier(List<String> issues) {
        return new BuildVerifier() {
            @Override
            public List<BuildIssue> verifyBuild() {
                return issues.stream()
                        .map(BuildIssue::issue)
                        .collect(Collectors.toList());

            }
        };
    }
}