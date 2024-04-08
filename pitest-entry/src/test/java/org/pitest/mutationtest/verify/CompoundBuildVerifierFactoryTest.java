package org.pitest.mutationtest.verify;

import org.junit.Test;
import org.mockito.Mockito;
import org.pitest.classpath.CodeSource;
import org.pitest.mutationtest.config.ReportOptions;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.pitest.mutationtest.verify.BuildMessage.buildMessage;

public class CompoundBuildVerifierFactoryTest {

    @Test
    public void returnsResultsFromChildren() {
        CompoundBuildVerifierFactory underTest = new CompoundBuildVerifierFactory(asList(
                factoryFor(buildVerifier(asList("one"))),
                factoryFor(buildVerifier(asList("two", "three")))));

        assertThat(underTest.create(new BuildVerifierArguments(aCodeSource(), new ReportOptions())).verifyBuild())
                .containsExactly(buildMessage("one"), buildMessage("two"), buildMessage("three"));
    }

    private BuildVerifierFactory factoryFor(BuildVerifier bv) {
        BuildVerifierFactory vs = Mockito.mock(BuildVerifierFactory.class);
        when(vs.create(any(BuildVerifierArguments.class))).thenReturn(bv);
        return vs;
    }

    private CodeSource aCodeSource() {
        return Mockito.mock(CodeSource.class);
    }

    private BuildVerifier buildVerifier(List<String> issues) {
        return new BuildVerifier() {
            @Override
            public List<BuildMessage> verifyBuild() {
                return issues.stream()
                        .map(BuildMessage::buildMessage)
                        .collect(Collectors.toList());

            }
        };
    }
}