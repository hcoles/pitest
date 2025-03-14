package org.pitest.mutationtest.engine.gregor;


import org.junit.Test;
import org.pitest.mutationtest.engine.MutationIdentifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.mutationtest.LocationMother.aLocation;

public class MethodMutatorFactoryWithInfoTest {

    @Test
    public void providesDocInfoWhenMutatorMatches() {
        MethodMutatorFactoryWithInfo underTest = testeeMatchingMutation(true);

        MutantUrl expected = new MutantUrl(UrlType.DOC, "http://www.example.com");
        assertThat(underTest.urlForMutant(aMutantId())).contains(expected);

    }

    @Test
    public void providesNoInfoWhenMutatorDoesntMatch() {
        MethodMutatorFactoryWithInfo underTest = testeeMatchingMutation(false);
        assertThat(underTest.urlForMutant(aMutantId())).isEmpty();

    }

    private static MutationIdentifier aMutantId() {
        return new MutationIdentifier(aLocation().build(), 1, "desc");
    }

    private MethodMutatorFactoryWithInfo testeeMatchingMutation(boolean matches) {
        return new MethodMutatorFactoryWithInfo() {
            @Override
            public String getGloballyUniqueId() {
                return "";
            }

            @Override
            public String getName() {
                return "";
            }

            @Override
            public String url() {
                return "http://www.example.com";
            }

            @Override
            public boolean isMutatorFor(MutationIdentifier mutationIdentifier) {
                return matches;
            }
        };
    }
}