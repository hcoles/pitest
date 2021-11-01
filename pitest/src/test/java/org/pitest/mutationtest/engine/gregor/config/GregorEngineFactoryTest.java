package org.pitest.mutationtest.engine.gregor.config;

import org.junit.Test;
import org.pitest.mutationtest.EngineArguments;
import org.pitest.mutationtest.engine.MutationEngine;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class GregorEngineFactoryTest  {

    GregorEngineFactory underTest = new GregorEngineFactory();

    @Test
    public void parsesSingleMutatorString() {
          MutationEngine actual = parseMutators(asList("NULL_RETURNS"));

        assertThat(actual.getMutatorNames()).contains("NULL_RETURNS");
    }

    @Test
    public void parsesGroups() {
        MutationEngine actual = parseMutators(asList("STRONGER"));

        assertThat(actual.getMutatorNames()).contains("NULL_RETURNS", "REMOVE_CONDITIONALS_EQUAL_ELSE");
    }

    @Test
    public void parsesExclusions() {
        MutationEngine excluded = parseMutators(asList("STRONGER", "-REMOVE_CONDITIONALS_EQUAL_ELSE"));

        assertThat(excluded.getMutatorNames()).doesNotContain("REMOVE_CONDITIONALS_EQUAL_ELSE");
        assertThat(excluded.getMutatorNames()).isNotEmpty();
    }

    @Test
    public void excludesGroups() {
        MutationEngine excluded = parseMutators(asList("ALL", "-ALL"));
        assertThat(excluded.getMutatorNames()).isEmpty();
    }

    private MutationEngine parseMutators(List<String> mutators) {
        return underTest.createEngine(EngineArguments.arguments().withMutators(mutators));
    }

}