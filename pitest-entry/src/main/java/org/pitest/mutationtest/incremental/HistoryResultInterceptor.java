package org.pitest.mutationtest.incremental;

import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.History;
import org.pitest.mutationtest.MutationResultInterceptor;

import java.util.Collection;

/**
 * Records results for history before other interceptors are applied. Artificially
 * added via hacky hard coding.
 */
public class HistoryResultInterceptor implements MutationResultInterceptor {

    private final History history;

    public HistoryResultInterceptor(History historyStore) {
        this.history = historyStore;
    }

    @Override
    public Collection<ClassMutationResults> modify(Collection<ClassMutationResults> results) {
        results.stream()
                .flatMap(c -> c.getMutations().stream())
                .forEach(this.history::recordResult);
        return results;
    }

    @Override
    public int priority() {
        return 0;
    }
}
