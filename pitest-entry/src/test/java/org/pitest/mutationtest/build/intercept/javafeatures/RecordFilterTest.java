package org.pitest.mutationtest.build.intercept.javafeatures;

import org.junit.Test;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.config.Mutator;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

public class RecordFilterTest {
    private static final String             PATH      = "records/{0}_{1}";

    RecordFilter testee = new RecordFilter();
    FilterTester verifier = new FilterTester(PATH, this.testee, Mutator.all());

    @Test
    public void shouldDeclareTypeAsFilter() {
        assertThat(this.testee.type()).isEqualTo(InterceptorType.FILTER);
    }

    @Test
    public void shouldFindNoMutantsInPureRecord() {
        this.verifier.assertLeavesNMutants(0, "PureRecord");
    }

    @Test
    public void shouldFindMutantsInNormalClass() {
        this.verifier.assertFiltersNoMutationsMatching(m -> true, NotARecord.class);
    }

    @Test
    public void shouldMutateNonRecordMethods() {
        this.verifier.assertFiltersNoMutationsMatching(inMethodCalled("extraMethod"), "CustomRecord");
    }

    private Predicate<MutationDetails> inMethodCalled(String name) {
        return m -> m.getMethod().name().equals(name);
    }

}