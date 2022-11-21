package org.pitest.mutationtest.build.intercept.exclude;

import junit.framework.TestCase;
import org.junit.Test;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationDetailsMother;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class FirstLineFilterTest {

    FirstLineFilter underTest = new FirstLineFilter();

    @Test
    public void doesNotFilterMutantsAfterFirstLine() {
        List<MutationDetails> mutants = MutationDetailsMother.aMutationDetail()
                .withLineNumber(2)
                .build(10);

        assertThat(underTest.intercept(mutants, null)).containsAll(mutants);
    }

    @Test
    public void filtersMutantsOnLine1() {
        MutationDetails line1 = MutationDetailsMother.aMutationDetail()
                .withLineNumber(1)
                .build();

        MutationDetails line2 = MutationDetailsMother.aMutationDetail()
                .withLineNumber(2)
                .build();

        assertThat(underTest.intercept(asList(line1, line2), null)).containsExactly(line2);
    }

    @Test
    public void filtersMutantsOnLine0() {
        MutationDetails line0 = MutationDetailsMother.aMutationDetail()
                .withLineNumber(0)
                .build();

        MutationDetails line2 = MutationDetailsMother.aMutationDetail()
                .withLineNumber(2)
                .build();

        assertThat(underTest.intercept(asList(line0, line2), null)).containsExactly(line2);
    }
}