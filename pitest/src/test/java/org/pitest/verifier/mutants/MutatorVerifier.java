package org.pitest.verifier.mutants;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verification based on class type only
 */
public class MutatorVerifier {

    private final GregorMutater engine;
    private final Class<?> clazz;
    private Predicate<MutationDetails> filter;

    public MutatorVerifier(GregorMutater engine, Class<?> clazz, Predicate<MutationDetails> filter) {
        this.engine = engine;
        this.clazz = clazz;
        this.filter = filter;
    }

    public void createsNMutants(int n) {
        assertThat(findMutations()).hasSize(n);
    }

    public void noMutantsCreated() {
        assertThat(findMutations()).isEmpty();
    }

    public void firstMutantIsDescribedAs(String expected) {
        List<MutationDetails> mutants = findMutations();
        assertThat(mutants)
                .describedAs("No mutations created")
                .isNotEmpty();
        assertThat(findMutations().get(0).getDescription()).isEqualTo(expected);
    }

    List<MutationDetails> findMutations() {
        return this.engine.findMutations(ClassName.fromClass(clazz)).stream()
                .filter(filter)
                .collect(Collectors.toList());
    }


}
