package org.pitest.verifier.interceptors;

import org.assertj.core.api.SoftAssertions;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class MutantVerifier {

    private final Sample sample;
    private final SoftAssertions softly = new SoftAssertions();
    private final List<MutationDetails> mutations;
    private final Collection<MutationDetails> afterFiltering;
    private final List<MutationDetails> filtered;

    private final Predicate<MutationDetails> match;

    MutantVerifier(Sample sample,
                   Predicate<MutationDetails> match,
                   List<MutationDetails> mutations,
                   Collection<MutationDetails> afterFiltering,
                   List<MutationDetails> filtered) {
        this.sample = sample;
        this.match = match;
        this.mutations = mutations;
        this.afterFiltering = afterFiltering;
        this.filtered = filtered;
    }

    public MutantVerifier mutantsAreGenerated() {
        softly.assertThat(mutations)
                .describedAs("No mutations generated matching predicate. There is a bug in the test " + sample.clazz)
                .anyMatch(match);
        return this;
    }

    public MutantVerifier allMutantsAreFiltered() {
        softly.assertThat(afterFiltering)
                .describedAs("Expected all mutants matching predicate to be filtered in class " + sample.clazz)
                .noneMatch(match);
        return this;
    }

    public MutantVerifier noMutantsAreFiltered() {
          softly.assertThat(filtered)
                .describedAs("Expected not to filter any mutants matching predicate in class " + sample.clazz)
                .noneMatch(match);
        return this;
    }

    @Deprecated
    public MutantVerifier nMutantsAreFiltered(int n) {
        softly.assertThat(afterFiltering)
                .describedAs("Expected to filter %d mutants from %s", n, sample.clazz)
                .hasSize(mutations.size() - n);
        return this;
    }

    @Deprecated
    public MutantVerifier mutantsFilteredAtNLocations(int n) {
        final Set<Loc> originalLocations = new LinkedHashSet<>();
        FCollection.mapTo(mutations, toLocation(sample.clazz), originalLocations);

        final Set<Loc> filteredLocations = new LinkedHashSet<>();
        FCollection.mapTo(afterFiltering, toLocation(sample.clazz), filteredLocations);

        softly.assertThat(filteredLocations)
                .describedAs("Expected to filter %d locations from the %d in %s", n, originalLocations.size(), sample.clazz)
                .hasSize(originalLocations.size() - n);
        return this;
    }

    private Function<MutationDetails, Loc> toLocation(final ClassTree tree) {
        return a -> {
            final MethodTree method = tree.method(a.getId().getLocation()).get();
            return new Loc(a.getInstructionIndex(), method.instruction(a.getInstructionIndex()));
        };
    }

    public void verify() {
        softly.assertAll();
    }

}
