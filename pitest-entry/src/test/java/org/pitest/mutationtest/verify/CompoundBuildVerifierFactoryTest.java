package org.pitest.mutationtest.verify;

import org.junit.Test;
import org.mockito.Mockito;
import org.pitest.classpath.CodeSource;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CompoundBuildVerifierFactoryTest {

    @Test
    public void returnsResultsFromChildren() {
        CompoundBuildVerifierFactory underTest = new CompoundBuildVerifierFactory(asList(
                factoryFor(() -> asList("one")),
                factoryFor(() -> asList("two", "three"))));

        assertThat(underTest.create(aCodeSource()).verify()).containsExactly("one", "two", "three");
    }

    private BuildVerifierFactory factoryFor(BuildVerifier bv) {
        BuildVerifierFactory vs = Mockito.mock(BuildVerifierFactory.class);
        when(vs.create(any(CodeSource.class))).thenReturn(bv);
        return vs;
    }

    private CodeSource aCodeSource() {
        return Mockito.mock(CodeSource.class);
    }
}