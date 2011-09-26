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
package org.pitest.mutationtest.report;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.pitest.Description;
import org.pitest.TestResult;
import org.pitest.classinfo.ClassInfo;
import org.pitest.extension.TestListener;
import org.pitest.functional.F;
import org.pitest.functional.F2;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.internal.IsolationUtils;
import org.pitest.mutationtest.CoverageDatabase;
import org.pitest.mutationtest.MutationResultList;
import org.pitest.mutationtest.instrument.MutationMetaData;
import org.pitest.mutationtest.instrument.UnRunnableMutationTestMetaData;
import org.pitest.util.FileUtil;

public class MutationHtmlReportListener implements TestListener {

  private final ResultOutputStrategy          outputStrategy;

  private final MutatorScores                 mutatorScores = new MutatorScores();
  private final long                          startTime;

  private final Collection<SourceLocator>     sourceRoots   = new HashSet<SourceLocator>();

  private final List<MutationTestSummaryData> summaryData   = new ArrayList<MutationTestSummaryData>();
  private final List<String>                  errors        = new ArrayList<String>();
  private final CoverageDatabase              coverage;

  public MutationHtmlReportListener(final CoverageDatabase coverage,
      final long startTime, final ResultOutputStrategy outputStrategy,
      final SourceLocator... locators) {
    this.coverage = coverage;
    this.outputStrategy = outputStrategy;
    this.startTime = startTime;
    this.sourceRoots.addAll(Arrays.asList(locators));
  }

  private void extractMetaData(final TestResult tr) {
    final Option<MutationMetaData> d = tr.getValue(MutationMetaData.class);
    if (d.hasSome()) {
      processMetaData(d.value());
    } else {
      final Option<UnRunnableMutationTestMetaData> unrunnable = tr
          .getValue(UnRunnableMutationTestMetaData.class);
      if (unrunnable.hasSome()) {
        processUnruntest(unrunnable.value());
      }
    }
  }

  private void processUnruntest(final UnRunnableMutationTestMetaData unrunnable) {
    this.errors.add(unrunnable.getReason());
  }

  private void processMetaData(final MutationMetaData value) {

    try {
      this.mutatorScores.registerResults(value.getMutations());

      final String css = FileUtil.readToString(IsolationUtils
          .getContextClassLoader().getResourceAsStream(
              "templates/mutation/style.css"));

      final int lineCoverage = calculateLineCoverage(value);

      final MutationTestSummaryData summaryData = new MutationTestSummaryData(
          value.getMutatedClass(), value.getTestClasses(),
          value.getPercentageMutationCoverage(), lineCoverage);
      collectSummaryData(summaryData);

      final String fileName = summaryData.getFileName();

      final Writer writer = this.outputStrategy.createWriterForFile(fileName);

      final StringTemplateGroup group = new StringTemplateGroup("mutation_test");
      final StringTemplate st = group
          .getInstanceOf("templates/mutation/mutation_report");
      st.setAttribute("css", css);
      st.setAttribute("summary", summaryData);

      st.setAttribute("tests", value.getTargettedTests());

      st.setAttribute("mutators", value.getConfig().getMutatorNames());

      final Collection<SourceFile> sourceFiles = createAnnotatedSoureFiles(value);

      st.setAttribute("sourceFiles", sourceFiles);
      st.setAttribute("mutatedClasses", value.getMutatedClass());

      // st.setAttribute("groups", groups);
      writer.write(st.toString());
      writer.close();

    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }

  private int calculateLineCoverage(final MutationMetaData value) {
    final long numberOfCoveredLines = this.coverage
        .getNumberOfCoveredLines(value.getMutatedClass());

    int lineCoverage = 0;
    if (numberOfCoveredLines != 0) {
      final long numberOfCodeLines = FCollection.fold(accumulateCodeLines(), 0,
          this.coverage.getClassInfo(value.getMutatedClass()));

      lineCoverage = Math
          .round(100f / numberOfCodeLines * numberOfCoveredLines);

    }
    return lineCoverage;
  }

  private F2<Integer, ClassInfo, Integer> accumulateCodeLines() {
    return new F2<Integer, ClassInfo, Integer>() {

      public Integer apply(final Integer a, final ClassInfo b) {
        return a + b.getCodeLines().size();
      }

    };
  }

  private Collection<SourceFile> createAnnotatedSoureFiles(
      final MutationMetaData value) throws IOException {
    final Collection<SourceFile> sourceFiles = new ArrayList<SourceFile>();
    for (final String each : value.getSourceFiles()) {
      final MutationResultList mutationsForThisFile = value
          .getResultsForSourceFile(each);
      final List<Line> lines = createAnnotatedSourceCodeLines(each,
          mutationsForThisFile,
          this.coverage.getClassInfo(value.getClassesForSourceFile(each)));

      sourceFiles.add(new SourceFile(each, lines, mutationsForThisFile
          .groupMutationsByLine()));
    }
    return sourceFiles;
  }

  private void collectSummaryData(final MutationTestSummaryData summaryData) {
    synchronized (this.summaryData) {
      this.summaryData.add(summaryData);
    }

  }

  private List<Line> createAnnotatedSourceCodeLines(final String sourceFile,
      final MutationResultList mutationsForThisFile,
      final Collection<ClassInfo> classes) throws IOException {
    final Option<Reader> reader = findSourceFile(classInfoToNames(classes),
        sourceFile);
    if (reader.hasSome()) {
      final AnnotatedLineFactory alf = new AnnotatedLineFactory(
          mutationsForThisFile, this.coverage, classes);
      return alf.convert(reader.value());
    }
    return Collections.emptyList();
  }

  private Collection<String> classInfoToNames(
      final Collection<ClassInfo> classes) {
    return FCollection.map(classes, classInfoToName());
  }

  private F<ClassInfo, String> classInfoToName() {
    return new F<ClassInfo, String>() {

      public String apply(final ClassInfo a) {
        return a.getName();
      }

    };
  }

  private Option<Reader> findSourceFile(final Collection<String> classes,
      final String fileName) {
    for (final SourceLocator each : this.sourceRoots) {
      final Option<Reader> maybe = each.locate(classes, fileName);
      if (maybe.hasSome()) {
        return maybe;
      }
    }
    return Option.none();
  }

  public void onTestError(final TestResult tr) {
    extractMetaData(tr);
  }

  public void onTestFailure(final TestResult tr) {
    extractMetaData(tr);
  }

  public void onTestSkipped(final TestResult tr) {
    extractMetaData(tr);
  }

  public void onTestStart(final Description d) {

  }

  public void onTestSuccess(final TestResult tr) {
    extractMetaData(tr);

  }

  public void onRunEnd() {
    try {
      final long duration = (System.currentTimeMillis() - this.startTime) / 1000;

      final StringTemplateGroup group = new StringTemplateGroup("mutation_test");
      final StringTemplate st = group
          .getInstanceOf("templates/mutation/mutation_index");

      final Writer writer = this.outputStrategy
          .createWriterForFile("index.html");

      st.setAttribute("summaryList", this.summaryData);
      st.setAttribute("errors", this.errors);
      st.setAttribute("numberOfMutations",
          this.mutatorScores.getTotalMutations());
      st.setAttribute("numberOfDetectedMutations",
          this.mutatorScores.getTotalDetectedMutations());
      st.setAttribute("duration", duration);
      st.setAttribute("mutatorScores", this.mutatorScores);
      writer.write(st.toString());
      writer.close();

    } catch (final IOException e) {
      e.printStackTrace();
    }

  }

  public void onRunStart() {

  }

}
