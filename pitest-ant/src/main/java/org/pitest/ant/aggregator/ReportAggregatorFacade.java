package org.pitest.ant.aggregator;


import org.apache.tools.ant.types.Path;
import org.pitest.aggregate.ReportAggregationException;

import java.util.List;


public interface ReportAggregatorFacade {
    void aggregateReport() throws ReportAggregationException;

    void setLineCoverageFiles(List<Path> lineCoverageFiles);

    void setMutationFiles(List<Path> mutationFiles);

    void setSourceCodeDirs(List<Path> sourceCodeDirs);

    void setCompiledCodeDirs(List<Path> compiledCodeDirs);
}
