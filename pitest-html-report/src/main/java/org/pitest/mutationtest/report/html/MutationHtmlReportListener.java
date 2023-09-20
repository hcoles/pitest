/*
 * Copyright 2010 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.report.html;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.pitest.coverage.ClassLines;
import org.pitest.coverage.ReportCoverage;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.SourceLocator;
import org.pitest.util.FileUtil;
import org.pitest.util.IsolationUtils;
import org.pitest.util.Log;
import org.pitest.util.ResultOutputStrategy;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class MutationHtmlReportListener implements MutationResultListener {

  private final ResultOutputStrategy      outputStrategy;

  private final Collection<SourceLocator> sourceRoots;

  private final PackageSummaryMap         packageSummaryData = new PackageSummaryMap();
  private final ReportCoverage            coverage;
  private final Set<String>               mutatorNames;

  private final String                    css;
  private final Charset                   outputCharset;
  private final boolean reportCoverage;

  public MutationHtmlReportListener(Charset outputCharset,
                                    ReportCoverage coverage,
                                    ResultOutputStrategy outputStrategy,
                                    Collection<String> mutatorNames,
                                    boolean reportCoverage,
                                    SourceLocator... locators) {
    this.outputCharset = outputCharset;
    this.coverage = coverage;
    this.outputStrategy = outputStrategy;
    this.sourceRoots = new HashSet<>(asList(locators));
    this.mutatorNames = new HashSet<>(mutatorNames);
    this.css = loadCss();
    this.reportCoverage = reportCoverage;
  }

  private String loadCss() {
    try {
      return FileUtil.readToString(IsolationUtils.getContextClassLoader()
          .getResourceAsStream("templates/mutation/style.css"));
    } catch (final IOException e) {
      Log.getLogger().log(Level.SEVERE, "Error while loading css", e);
    }
    return "";
  }

  private void generateAnnotatedSourceFile(
      final MutationTestSummaryData mutationMetaData) {


    final String fileName = mutationMetaData.getPackageName()
        + File.separator + mutationMetaData.getFileName() + ".html";

    try (Writer writer = this.outputStrategy.createWriterForFile(fileName)) {

      final StringTemplateGroup group = new StringTemplateGroup("mutation_test");
      final StringTemplate st = group
          .getInstanceOf("templates/mutation/mutation_report");
      st.setAttribute("css", this.css);

      st.setAttribute("tests", mutationMetaData.getTests());

      st.setAttribute("mutators", mutationMetaData.getMutators());

      final SourceFile sourceFile = createAnnotatedSourceFile(mutationMetaData);

      st.setAttribute("sourceFile", sourceFile);
      st.setAttribute("mutatedClasses", mutationMetaData.getMutatedClasses());
      st.setAttribute("outputCharset", this.outputCharset);
      st.setAttribute("showCoverage", this.reportCoverage);
      writer.write(st.toString());


    } catch (final IOException ex) {
      Log.getLogger().log(Level.WARNING, "Error while writing report", ex);
    }
  }

  private PackageSummaryData collectPackageSummaries(
      final ClassMutationResults mutationMetaData) {
    final String packageName = mutationMetaData.getPackageName();

    return this.packageSummaryData.update(packageName,
        createSummaryData(this.coverage, mutationMetaData));
  }

  public MutationTestSummaryData createSummaryData(
      final ReportCoverage coverage, final ClassMutationResults data) {

    final List<ClassLines> lines = asList(this.coverage.getCodeLinesForClass(data.getMutatedClass()));

    return new MutationTestSummaryData(data.getFileName(), data.getMutations(),
        this.mutatorNames, lines, coverage.getCoveredLines(data.getMutatedClass()).size());
  }

  private SourceFile createAnnotatedSourceFile(
      final MutationTestSummaryData mutationMetaData) throws IOException {

    final String fileName = mutationMetaData.getFileName();
    final String packageName = mutationMetaData.getPackageName();

    final MutationResultList mutationsForThisFile = mutationMetaData
        .getResults();

    final List<Line> lines = createAnnotatedSourceCodeLines(fileName,
        packageName, mutationsForThisFile);

    return new SourceFile(fileName, lines,
        mutationsForThisFile.groupMutationsByLine());
  }

  private List<Line> createAnnotatedSourceCodeLines(final String sourceFile,
      final String packageName, final MutationResultList mutationsForThisFile)
          throws IOException {
    final Collection<ClassLines> classes = this.coverage.getClassesForFile(
        sourceFile, packageName);

    final Optional<Reader> reader = findSourceFile(classInfoToNames(classes),
        sourceFile);
    if (reader.isPresent()) {
      final AnnotatedLineFactory alf = new AnnotatedLineFactory(
          mutationsForThisFile.list(), this.coverage, classes, this.reportCoverage);
      return alf.convert(reader.get());
    }
    return Collections.emptyList();
  }

  private Collection<String> classInfoToNames(Collection<ClassLines> classes) {
    return classes.stream()
            .map(classInfoToJavaName())
            .collect(Collectors.toList());
  }

  private Function<ClassLines, String> classInfoToJavaName() {
    return a -> a.name().asJavaName();
  }

  private Optional<Reader> findSourceFile(final Collection<String> classes,
      final String fileName) {
    for (final SourceLocator each : this.sourceRoots) {
      final Optional<Reader> maybe = each.locate(classes, fileName);
      if (maybe.isPresent()) {
        return maybe;
      }
    }
    return Optional.empty();
  }

  public void onRunEnd() {
    createIndexPages();
    createCssFile();
  }

  private void createCssFile() {
    final Writer cssWriter = this.outputStrategy.createWriterForFile("style.css");
    try {
      cssWriter.write(this.css);
      cssWriter.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private void createIndexPages() {

    final StringTemplateGroup group = new StringTemplateGroup("mutation_test");
    final StringTemplate st = group
        .getInstanceOf("templates/mutation/mutation_package_index");

    final Writer writer = this.outputStrategy.createWriterForFile("index.html");
    final MutationTotals totals = new MutationTotals();

    final List<PackageSummaryData> psd = new ArrayList<>(
        this.packageSummaryData.values());
    Collections.sort(psd);
    for (final PackageSummaryData psData : psd) {
      totals.add(psData.getTotals());
      createPackageIndexPage(psData);
    }

    st.setAttribute("totals", totals);
    st.setAttribute("packageSummaries", psd);
    st.setAttribute("outputCharset", this.outputCharset);
    st.setAttribute("showCoverage", this.reportCoverage);
    try {
      writer.write(st.toString());
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }

  }

  private void createPackageIndexPage(final PackageSummaryData psData) {
    final StringTemplateGroup group = new StringTemplateGroup("mutation_test");
    final StringTemplate st = group
        .getInstanceOf("templates/mutation/package_index");

    final Writer writer = this.outputStrategy.createWriterForFile(psData
        .getPackageDirectory() + File.separator + "index.html");
    st.setAttribute("packageData", psData);
    st.setAttribute("outputCharset", this.outputCharset);
    st.setAttribute("showCoverage", this.reportCoverage);
    try {
      writer.write(st.toString());
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }

  }

  @Override
  public void runStart() {
    // TODO Auto-generated method stub

  }

  @Override
  public void runEnd() {
    createIndexPages();
    createCssFile();
  }

  @Override
  public void handleMutationResult(final ClassMutationResults metaData) {
    final PackageSummaryData packageData = collectPackageSummaries(metaData);

    generateAnnotatedSourceFile(packageData.getForSourceFile(metaData
        .getFileName()));

  }

}