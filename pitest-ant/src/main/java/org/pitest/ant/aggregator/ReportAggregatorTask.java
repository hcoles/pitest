package org.pitest.ant.aggregator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.pitest.aggregate.ReportAggregationException;

import java.util.ArrayList;
import java.util.List;


public class ReportAggregatorTask extends Task {

    private List<Path> lineCoverageFilePaths = new ArrayList<>();
    private List<Path> mutationFiles = new ArrayList<>();
    private List<Path> sourceCodeDirs = new ArrayList<>();
    private List<Path> compiledCodeDirs = new ArrayList<>();

    @Override
    public void execute() {
        try {
            execute(new ReportAggregatorFacadeImpl());
        } catch (ReportAggregationException e) {
            throw new BuildException(e);
        }
    }

    void execute(ReportAggregatorFacade aggregator) throws ReportAggregationException {

        aggregator.setLineCoverageFiles(lineCoverageFilePaths);
        aggregator.setSourceCodeDirs(sourceCodeDirs);
        aggregator.setMutationFiles(mutationFiles);
        aggregator.setCompiledCodeDirs(compiledCodeDirs);
        aggregator.aggregateReport();
    }

    public void addLineCoverageFiles(Path path) {
        this.lineCoverageFilePaths.add(path);
    }

    public void addMutationFiles(Path path) {
        this.mutationFiles.add(path);
    }

    public void addSourceCodeDirs(Path path) {
        this.sourceCodeDirs.add(path);
    }

    public void addCompiledCodeDirs(Path path) {
        this.compiledCodeDirs.add(path);
    }
}
