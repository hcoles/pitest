package org.pitest.mutationtest;

import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.HierarchicalClassId;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public interface History {

    void initialize();
    default Predicate<ClassName> limitTests() {
        return c -> true;
    }
    void processCoverage(CoverageDatabase coverageData);

    void recordClassPath(Collection<HierarchicalClassId> ids, CoverageDatabase coverageInfo);

    List<MutationResult> analyse(List<MutationDetails> mutationsForClasses);

    void recordResult(MutationResult result);

    void close();

}
