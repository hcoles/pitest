package org.pitest.ant.aggregator;

import org.apache.tools.ant.types.Path;
import org.pitest.aggregate.ReportAggregationException;
import org.pitest.aggregate.ReportAggregator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;


public class ReportAggregatorFacadeImpl implements ReportAggregatorFacade {
    private List<Path> lineCoverageFiles = new ArrayList<>();
    private List<Path> mutationFiles = new ArrayList<>();
    private List<Path> sourceCodeDirs = new ArrayList<>();
    private List<Path> compiledCodeDirs = new ArrayList<>();

    @Override
    public void aggregateReport() throws ReportAggregationException {
        aggregateReport(ReportAggregator.builder()).aggregateReport();
    }

    ReportAggregator aggregateReport(ReportAggregator.Builder builder) {
        setParameter(lineCoverageFiles, builder::addLineCoverageFile);
        setParameter(mutationFiles, builder::addMutationResultsFile);
        setParameter(sourceCodeDirs, builder::addSourceCodeDirectory);
        setParameter(compiledCodeDirs, builder::addCompiledCodeDirectory);
        return builder.build();
    }

    private void setParameter(List<Path> lineCoverageFiles, Consumer<File> pathConsumer)
    {
        lineCoverageFiles
                .stream()
                .map(Path::list)
                .map(Arrays::asList)
                .flatMap(List::stream)
                .map(File::new)
                .forEach(pathConsumer);
    }

    @Override
    public void setLineCoverageFiles(List<Path> lineCoverageFiles) {
        this.lineCoverageFiles = lineCoverageFiles;
    }

    @Override
    public void setMutationFiles(List<Path> mutationFiles) {
        this.mutationFiles = mutationFiles;
    }

    @Override
    public void setSourceCodeDirs(List<Path> sourceCodeDirs) {
        this.sourceCodeDirs = sourceCodeDirs;
    }

    @Override
    public void setCompiledCodeDirs(List<Path> compiledCodeDirs) {
        this.compiledCodeDirs = compiledCodeDirs;
    }
}
