package org.pitest.mutationtest.build;

import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DryRunUnit implements MutationAnalysisUnit {
    private static final Logger LOG = Log.getLogger();

    private final Collection<MutationDetails> mutations;

    public DryRunUnit(final Collection<MutationDetails> mutations) {
        this.mutations = mutations;
    }

    @Override
    public MutationMetaData call() throws Exception {
        LOG.fine("Not analysing " + this.mutations.size()
                + " mutations as in dry run");
        List<MutationResult> results = mutations.stream()
                .map(m -> new MutationResult(m, noAnalysis()))
                .collect(Collectors.toList());
        return new MutationMetaData(results);

    }

    private MutationStatusTestPair noAnalysis() {
        return MutationStatusTestPair.notAnalysed(0, DetectionStatus.NOT_STARTED, Collections.emptyList());
    }

    @Override
    public int priority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Collection<MutationDetails> mutants() {
        return mutations;
    }

}
