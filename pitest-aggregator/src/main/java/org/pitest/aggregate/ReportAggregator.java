package org.pitest.aggregate;

import org.pitest.classpath.CodeSource;
import org.pitest.coverage.BlockCoverage;
import org.pitest.coverage.BlockLocation;
import org.pitest.coverage.CoverageData;
import org.pitest.coverage.ReportCoverage;
import org.pitest.coverage.analysis.LineMapper;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.SourceLocator;
import org.pitest.mutationtest.report.html.MutationHtmlReportListener;
import org.pitest.mutationtest.report.xml.XMLReportListener;
import org.pitest.mutationtest.tooling.SmartSourceLocator;
import org.pitest.util.Log;
import org.pitest.util.ResultOutputStrategy;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class ReportAggregator {
  private final ResultOutputStrategy       resultOutputStrategy;
  private final DataLoader<BlockCoverage>  blockCoverageLoader;
  private final DataLoader<MutationResult> mutationLoader;

  private final Collection<File>           sourceCodeDirectories;
  private final CodeSourceAggregator       codeSourceAggregator;
  private final Charset inputCharset;
  private final Charset outputCharset;

  private ReportAggregator(final ResultOutputStrategy resultOutputStrategy, final Set<File> lineCoverageFiles, final Set<File> mutationFiles,
                           final Set<File> sourceCodeDirs, final Set<File> compiledCodeDirs, Charset inputCharset, Charset outputCharset) {
    this.resultOutputStrategy = resultOutputStrategy;
    this.blockCoverageLoader = new BlockCoverageDataLoader(lineCoverageFiles);
    this.mutationLoader = new MutationResultDataLoader(mutationFiles);
    this.sourceCodeDirectories = Collections.unmodifiableCollection(new HashSet<>(sourceCodeDirs));
    this.codeSourceAggregator = new CodeSourceAggregator(new HashSet<>(compiledCodeDirs));
    this.inputCharset = inputCharset;
    this.outputCharset = outputCharset;
  }

  public void aggregateReport() throws ReportAggregationException {
    final MutationMetaData mutationMetaData = new MutationMetaData(new ArrayList<>(this.mutationLoader.loadData()));

    final MutationResultListener mutationResultListener = createResultListener(mutationMetaData);

    aggregateReport(mutationMetaData, mutationResultListener);
  }

  public void aggregateXmlReport(boolean fullMutationMatrix) throws ReportAggregationException {
    final MutationMetaData mutationMetaData =
        new MutationMetaData(new ArrayList<>(this.mutationLoader.loadData()));

    final MutationResultListener mutationResultListener =
        new XMLReportListener(this.resultOutputStrategy, fullMutationMatrix);

    aggregateReport(mutationMetaData, mutationResultListener);
  }

  private void aggregateReport(
      MutationMetaData mutationMetaData,
      MutationResultListener mutationResultListener
  ) {
    mutationResultListener.runStart();

    for (final ClassMutationResults mutationResults : mutationMetaData.toClassResults()) {
      mutationResultListener.handleMutationResult(mutationResults);
    }
    mutationResultListener.runEnd();
  }

  private MutationResultListener createResultListener(final MutationMetaData mutationMetaData) throws ReportAggregationException {
    final SourceLocator sourceLocator = new SmartSourceLocator(this.sourceCodeDirectories, inputCharset);

    final CodeSource codeSource = this.codeSourceAggregator.createCodeSource();
    final ReportCoverage coverageDatabase = calculateCoverage(codeSource);
    final Collection<String> mutatorNames = new HashSet<>(FCollection.flatMap(mutationMetaData.getMutations(), resultToMutatorName()));

    return new MutationHtmlReportListener(outputCharset, coverageDatabase, this.resultOutputStrategy, mutatorNames, sourceLocator);
  }

  private static Function<MutationResult, List<String>> resultToMutatorName() {
    return a -> {
      try {
        final String mutatorName = a.getDetails().getId().getMutator();//MutatorUtil.loadMutator(a.getDetails().getMutator()).getName();
        return Collections.singletonList(mutatorName);
      } catch (final Exception e) {
        throw new RuntimeException("Cannot convert to mutator: " + a.getDetails().getMutator(), e);
      }
    };
  }

  private ReportCoverage calculateCoverage(final CodeSource codeSource) throws ReportAggregationException {
    try {
      Collection<BlockLocation> coverageData = this.blockCoverageLoader.loadData().stream()
              .map(BlockCoverage::getBlock)
              .collect(Collectors.toList());
      CoverageData cd = new CoverageData(codeSource, new LineMapper(codeSource));
      cd.loadBlockDataOnly(coverageData);
      return cd;
    } catch (final Exception e) {
      throw new ReportAggregationException(e.getMessage(), e);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private static final Logger  LOG = Log.getLogger();

    private ResultOutputStrategy resultOutputStrategy;
    private final Set<File>      lineCoverageFiles       = new HashSet<>();
    private final Set<File>      mutationResultsFiles    = new HashSet<>();
    private final Set<File>      sourceCodeDirectories   = new HashSet<>();
    private final Set<File>      compiledCodeDirectories = new HashSet<>();
    private Charset inputCharset = Charset.defaultCharset();
    private Charset outputCharset = Charset.defaultCharset();

    public Builder inputCharSet(Charset inputCharset) {
      this.inputCharset = inputCharset;
      return this;
    }

    public Builder outputCharset(Charset outputCharset) {
      this.outputCharset = outputCharset;
      return this;
    }

    public Builder resultOutputStrategy(final ResultOutputStrategy resultOutputStrategy) {
      this.resultOutputStrategy = resultOutputStrategy;
      return this;
    }

    public Builder lineCoverageFiles(final List<File> lineCoverageFiles) {
      this.lineCoverageFiles.clear();
      for (final File file : lineCoverageFiles) {
        addLineCoverageFile(file);
      }
      return this;
    }

    public Builder addLineCoverageFile(final File lineCoverageFile) {
      validateFile(lineCoverageFile);
      this.lineCoverageFiles.add(lineCoverageFile);
      return this;
    }

    public Builder mutationResultsFiles(final List<File> mutationResultsFiles) {
      this.mutationResultsFiles.clear();
      for (final File file : mutationResultsFiles) {
        addMutationResultsFile(file);
      }
      return this;
    }

    public Builder addMutationResultsFile(final File mutationResultsFile) {
      validateFile(mutationResultsFile);
      this.mutationResultsFiles.add(mutationResultsFile);
      return this;
    }

    public Builder sourceCodeDirectories(final List<File> sourceCodeDirectories) {
      this.sourceCodeDirectories.clear();
      for (final File file : sourceCodeDirectories) {
        addSourceCodeDirectory(file);
      }
      return this;
    }

    public Builder addSourceCodeDirectory(final File sourceCodeDirectory) {
      validateDirectory(sourceCodeDirectory);
      if (sourceCodeDirectory.exists()) {
        this.sourceCodeDirectories.add(sourceCodeDirectory);
      } else {
        LOG.info("ignoring absent source code directory " + sourceCodeDirectory.getAbsolutePath());
      }
      return this;
    }

    public Builder compiledCodeDirectories(final List<File> compiledCodeDirectories) {
      this.compiledCodeDirectories.clear();
      for (final File file : compiledCodeDirectories) {
        addCompiledCodeDirectory(file);
      }
      return this;
    }

    public Builder addCompiledCodeDirectory(final File compiledCodeDirectory) {
      validateDirectory(compiledCodeDirectory);
      if (compiledCodeDirectory.exists()) {
        this.compiledCodeDirectories.add(compiledCodeDirectory);
      } else {
        LOG.info("ignoring absent compiled code directory " + compiledCodeDirectory.getAbsolutePath());
      }
      return this;
    }

    public Set<File> getCompiledCodeDirectories() {
      return this.compiledCodeDirectories;
    }

    public Set<File> getLineCoverageFiles() {
      return this.lineCoverageFiles;
    }

    public Set<File> getMutationResultsFiles() {
      return this.mutationResultsFiles;
    }

    public Set<File> getSourceCodeDirectories() {
      return this.sourceCodeDirectories;
    }

    public Charset getInputCharSet() {
      return this.inputCharset;
    }

    public Charset getOutputCharSet() {
      return this.outputCharset;
    }

    public ReportAggregator build() {
      validateState();
      return new ReportAggregator(this.resultOutputStrategy,
              this.lineCoverageFiles,
              this.mutationResultsFiles,
              this.sourceCodeDirectories,
              this.compiledCodeDirectories,
              inputCharset,
              outputCharset);
    }

    /*
     * Validators
     */
    private void validateState() {
      if (this.resultOutputStrategy == null) {
        throw new IllegalStateException("Failed to build: the resultOutputStrategy has not been set");
      }
      if (this.lineCoverageFiles.isEmpty()) {
        throw new IllegalStateException("Failed to build: no lineCoverageFiles have been set");
      }
      if (this.mutationResultsFiles.isEmpty()) {
        throw new IllegalStateException("Failed to build: no mutationResultsFiles have been set");
      }
      if (this.sourceCodeDirectories.isEmpty()) {
        throw new IllegalStateException("Failed to build: no sourceCodeDirectories have been set");
      }
      if (this.compiledCodeDirectories.isEmpty()) {
        throw new IllegalStateException("Failed to build: no compiledCodeDirectories have been set");
      }
    }

    private void validateFile(final File file) {
      if (file == null) {
        throw new IllegalArgumentException("file is null");
      }
      if (!file.exists() || !file.isFile()) {
        throw new IllegalArgumentException(file.getAbsolutePath() + " does not exist or is not a file");
      }
    }

    private void validateDirectory(final File directory) {
      if (directory == null) {
        throw new IllegalArgumentException("directory is null");
      }
      // For this method, a non existing directory is valid.
      // It probably needs some special treatment later, but it shouldn't prevent the aggregator to be built.
      if (directory.exists() && !directory.isDirectory()) {
        throw new IllegalArgumentException(directory.getAbsolutePath() + " is not a directory");
      }
    }
  }
}
