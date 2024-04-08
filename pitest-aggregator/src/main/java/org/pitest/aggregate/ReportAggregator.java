package org.pitest.aggregate;

import org.pitest.classpath.CodeSource;
import org.pitest.coverage.BlockCoverage;
import org.pitest.coverage.BlockLocation;
import org.pitest.coverage.CoverageData;
import org.pitest.coverage.ReportCoverage;
import org.pitest.coverage.analysis.LineMapper;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.SourceLocator;
import org.pitest.mutationtest.build.CoverageTransformer;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.mutationtest.report.html.MutationHtmlReportListener;
import org.pitest.mutationtest.tooling.SmartSourceLocator;
import org.pitest.util.Log;
import org.pitest.util.ResultOutputStrategy;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public final class ReportAggregator {

  private final SettingsFactory settings;
  private final ResultOutputStrategy       resultOutputStrategy;
  private final DataLoader<BlockCoverage>  blockCoverageLoader;
  private final Set<File> mutationFiles;

  private final Collection<File>           sourceCodeDirectories;
  private final CodeSourceAggregator       codeSourceAggregator;
  private final Charset inputCharset;
  private final Charset outputCharset;

  private ReportAggregator(SettingsFactory settings,
                           ResultOutputStrategy resultOutputStrategy,
                           Set<File> lineCoverageFiles,
                           Set<File> mutationFiles,
                           Set<File> sourceCodeDirs,
                           Set<File> compiledCodeDirs,
                           Charset inputCharset,
                           Charset outputCharset) {
    this.settings = settings;
    this.resultOutputStrategy = resultOutputStrategy;
    this.blockCoverageLoader = new BlockCoverageDataLoader(lineCoverageFiles);
    this.mutationFiles = mutationFiles;
    this.sourceCodeDirectories = Collections.unmodifiableCollection(new HashSet<>(sourceCodeDirs));
    this.codeSourceAggregator = new CodeSourceAggregator(settings, new HashSet<>(compiledCodeDirs));
    this.inputCharset = inputCharset;
    this.outputCharset = outputCharset;
  }

  public AggregationResult aggregateReport() throws ReportAggregationException {
    SmartSourceLocator sourceLocator = new SmartSourceLocator(asPaths(this.sourceCodeDirectories), inputCharset);

    boolean partialCoverage = scanForPartialCoverageFlag(mutationFiles);

    final MutationResultListener mutationResultListener = createResultListener(sourceLocator, Collections.emptySet(), partialCoverage);
    final ReportAggregatorResultListener reportAggregatorResultListener = new ReportAggregatorResultListener();

    reportAggregatorResultListener.runStart();
    mutationResultListener.runStart();

    for (File file : mutationFiles) {

      // hack so only source files from within a given module are resolved
      sourceLocator.sourceRootHint(file.getParentFile().toPath());

      MutationResultDataLoader loader = new MutationResultDataLoader(asList(file));
      MutationMetaData mutationMetaData = new MutationMetaData(new ArrayList<>(loader.loadData()));
      for (ClassMutationResults classResult : mutationMetaData.toClassResults()) {
        reportAggregatorResultListener.handleMutationResult(classResult);
        mutationResultListener.handleMutationResult(classResult);
      }

    }
    reportAggregatorResultListener.runEnd();
    mutationResultListener.runEnd();

    return reportAggregatorResultListener.result();
  }

  private boolean scanForPartialCoverageFlag(Set<File> mutationFiles) {
    // scan for the partial coverage flag. All files should have the same flag, so only really
    // need to scan the first line of the first file, but expressing as a loop here just because.
    for (File each : mutationFiles) {
      try (Stream<String> lines = Files.lines(each.toPath())) {
        // search for a false flag as this is the most likely case
        if (lines.anyMatch(l -> l.contains("<mutations partial=\"false\"")) ) {
          return false;
        }
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    return true;
  }

  private MutationResultListener createResultListener(SourceLocator sourceLocator, Collection<String> mutatorNames, boolean partialCoverage) throws ReportAggregationException {
    final CodeSource codeSource = this.codeSourceAggregator.createCodeSource();
    final ReportCoverage coverageDatabase = calculateCoverage(codeSource);

    return new MutationHtmlReportListener(outputCharset,
            coverageDatabase,
            this.resultOutputStrategy,
            mutatorNames,
            partialCoverage,
            Collections.emptyList(),
            true,
            sourceLocator);
  }

  private Collection<Path> asPaths(Collection<File> files) {
    return files.stream()
            .map(File::toPath)
            .collect(Collectors.toList());
  }

  private ReportCoverage calculateCoverage(final CodeSource codeSource) throws ReportAggregationException {
    try {
      Collection<BlockLocation> coverageData = this.blockCoverageLoader.loadData().stream()
              .map(BlockCoverage::getBlock)
              .collect(Collectors.toList());
      CoverageData cd = new CoverageData(codeSource, new LineMapper(codeSource));
      cd.loadBlockDataOnly(coverageData);

      return transformCoverage(cd);

    } catch (final Exception e) {
      throw new ReportAggregationException(e.getMessage(), e);
    }
  }

  private ReportCoverage transformCoverage(CoverageData cd) {
    CoverageTransformer transformer = settings.createCoverageTransformer(codeSourceAggregator.createCodeSource());
    return transformer.transform(cd);
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
      if (lineCoverageFile.exists()) {
        this.lineCoverageFiles.add(lineCoverageFile);
      } else {
        LOG.info("ignoring absent line coverage file " + lineCoverageFile.getAbsolutePath());
      }
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
      if (mutationResultsFile.exists()) {
        this.mutationResultsFiles.add(mutationResultsFile);
      } else {
        LOG.info("ignoring absent mutation results file " + mutationResultsFile.getAbsolutePath());
      }
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
      final SettingsFactory settings = new SettingsFactory(new ReportOptions(), PluginServices.makeForContextLoader());
      return new ReportAggregator(
              settings,
              this.resultOutputStrategy,
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
      // a non existent file shouldn't prevent the aggregator from being built
      if (file.exists() && !file.isFile()) {
        throw new IllegalArgumentException(file.getAbsolutePath() + "is not a file");
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
