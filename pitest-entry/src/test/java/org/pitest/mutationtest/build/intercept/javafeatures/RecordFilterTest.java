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
    public void declaresTypeAsFilter() {
        assertThat(this.testee.type()).isEqualTo(InterceptorType.FILTER);
    }

    @Test
    public void findsNoMutantsInEmptyRecord() {
        this.verifier.assertLeavesNMutants(0, "NoDataRecord");
    }

    @Test
    public void findsNoMutantsInPureRecord() {
        this.verifier.assertLeavesNMutants(0, "PureRecord");
    }

    @Test
    public void findsMutantsInNormalClass() {
        this.verifier.assertFiltersNoMutationsMatching(m -> true, NotARecord.class);
    }

    @Test
    public void mutatesNonRecordMethods() {
        this.verifier.assertFiltersNoMutationsMatching(inMethodCalled("extraMethod"), "CustomRecord");
    }

    @Test
    public void mutatesCustomConstructors() {
        this.verifier.assertFiltersNoMutationsMatching(inMethodCalled("<init>").and(removesSysOutCall()),
                "RecordWithCustomConstructor");
    }

    @Test
    public void mutatesCustomEqualsMethods() {
        this.verifier.assertFiltersNoMutationsMatching(inMethodCalled("equals"),
                "RecordWithCustomEquals");
    }

    @Test
    public void mutatesCustomHashCodeMethods() {
        this.verifier.assertFiltersNoMutationsMatching(inMethodCalled("hashCode"),
                "RecordWithCustomHashCode");
    }

    @Test
    public void mutatesCustomToStringMethods() {
        this.verifier.assertFiltersNoMutationsMatching(inMethodCalled("toString"),
                "RecordWithCustomToString");
    }

    private Predicate<MutationDetails> removesSysOutCall() {
        return m -> m.getDescription().contains("PrintStream::println");
    }

    private Predicate<MutationDetails> inMethodCalled(String name) {
        return m -> m.getMethod().equals(name);
    }

}