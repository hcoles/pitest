package org.pitest.mutationtest.incremental;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.History;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.report.MutationTestResultMother;

import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class HistoryResultInterceptorTest {
    private History store = Mockito.mock(History.class);
    private HistoryResultInterceptor testee = new HistoryResultInterceptor(this.store);

    @Test
    public void recordsMutationResults() {
        final MutationResult mr = makeResult();
        final ClassMutationResults metaData = MutationTestResultMother
                .createClassResults(mr);
        Collection<ClassMutationResults> mutants = asList(metaData);
        Collection<ClassMutationResults> actual = this.testee.modify(mutants);
        verify(this.store).recordResult(mr);
        assertThat(actual).isSameAs(mutants);
    }


    private MutationResult makeResult() {
        return new MutationResult(
                MutationTestResultMother.createDetails(), MutationStatusTestPair.notAnalysed(0,
                DetectionStatus.KILLED, Collections.emptyList()));
    }

}