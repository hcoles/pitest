package org.pitest.mutationtest;

import org.junit.Test;
import org.pitest.mutationtest.report.MutationTestResultMother;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.mutationtest.engine.MutationDetailsMother.aMutationDetail;
import static org.pitest.mutationtest.report.MutationTestResultMother.aMutationTestResult;

public class CompoundMutationResultInterceptorTest {

    @Test
    public void chainsChildCallsToModifyByPriority() {
        MutationResultInterceptor a = appendToDesc("bar", 1);
        MutationResultInterceptor b = appendToDesc("foo", 0);

        CompoundMutationResultInterceptor underTest = new CompoundMutationResultInterceptor(asList(a,b));

        Collection<ClassMutationResults> actual = underTest.modify(someClassResults());

        assertThat(actual.stream().flatMap(c -> c.getMutations().stream()))
                .allMatch(m -> m.getDetails().getDescription().endsWith("foobar"));
    }


    @Test
    public void combinesRemainingResults() {
        MutationResultInterceptor a = hasResult(aMutationTestResult().withMutationDetails(aMutationDetail().withDescription("a")));
        MutationResultInterceptor b = hasResult(aMutationTestResult().withMutationDetails(aMutationDetail().withDescription("b")));

        CompoundMutationResultInterceptor underTest = new CompoundMutationResultInterceptor(asList(a,b));

        Collection<ClassMutationResults> actual = underTest.remaining();

        assertThat(actual.stream().flatMap(c -> c.getMutations().stream().map(m -> m.getDetails().getDescription())))
                .containsExactly("a", "b");
    }

    private MutationResultInterceptor hasResult(MutationTestResultMother.MutationTestResultBuilder b) {
        return new MutationResultInterceptor() {
            @Override
            public Collection<ClassMutationResults> modify(Collection<ClassMutationResults> results) {
                return null;
            }

            @Override
            public Collection<ClassMutationResults> remaining() {
                return asList(new ClassMutationResults(asList(b.build())));
            }
        };
    }

    private static List<ClassMutationResults> someClassResults() {
        return asList(MutationTestResultMother.createClassResults(aMutationTestResult().build(2)));
    }

    private MutationResultInterceptor appendToDesc(final String foo, final int priority) {
        return new MutationResultInterceptor() {
            @Override
            public Collection<ClassMutationResults> modify(Collection<ClassMutationResults> results) {
                return results.stream()
                        .map(r -> new ClassMutationResults(r.getMutations().stream().map(m -> appendToDesc(m, foo)).collect(Collectors.toList())))
                        .collect(Collectors.toList());
            }

            @Override
            public int priority() {
                return priority;
            }

            private MutationResult appendToDesc(MutationResult result, String toAppend) {
                return new MutationResult(result.getDetails().withDescription(result.getDetails().getDescription() + toAppend)
                        , result.getStatusTestPair());
            }
        };
    }

}