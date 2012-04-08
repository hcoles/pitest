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

import java.io.File;
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

  private final ResultOutputStrategy            outputStrategy;

  private final MutatorScores                   mutatorScores      = new MutatorScores();

  private final Collection<SourceLocator>       sourceRoots        = new HashSet<SourceLocator>();

  private final PackageSummaryMap packageSummaryData = new PackageSummaryMap();
  private final List<String>                    errors             = new ArrayList<String>();
  private final CoverageDatabase                coverage;

  public MutationHtmlReportListener(final CoverageDatabase coverage,
      final long startTime, final ResultOutputStrategy outputStrategy,
      final SourceLocator... locators) {
    this.coverage = coverage;
    this.outputStrategy = outputStrategy;
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

  private void processMetaData(final MutationMetaData mutationMetaData) {

    try {
      this.mutatorScores.registerResults(mutationMetaData.getMutations());

      final String css = FileUtil.readToString(IsolationUtils
          .getContextClassLoader().getResourceAsStream(
              "templates/mutation/style.css"));
      collectPackageSummaries(mutationMetaData);
      final String fileName = (getPackageName(mutationMetaData)
          + File.separator + mutationMetaData.getFirstFileName()).replace('.',
          '_') + ".html";

      final Writer writer = this.outputStrategy.createWriterForFile(fileName);

      final StringTemplateGroup group = new StringTemplateGroup("mutation_test");
      final StringTemplate st = group
          .getInstanceOf("templates/mutation/mutation_report");
      st.setAttribute("css", css);

      st.setAttribute("tests", mutationMetaData.getTargettedTests());

      st.setAttribute("mutators", mutationMetaData.getConfig()
          .getMutatorNames());

      final Collection<SourceFile> sourceFiles = createAnnotatedSoureFiles(mutationMetaData);

      st.setAttribute("sourceFiles", sourceFiles);
      st.setAttribute("mutatedClasses", mutationMetaData.getMutatedClass());

      // st.setAttribute("groups", groups);
      writer.write(st.toString());
      writer.close();

    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }

  private void collectPackageSummaries(MutationMetaData mutationMetaData) {
    final MutationTotals totals = new MutationTotals();
    totals.addClasses(1); // FIXME assumes 1 new top level class per meta data
    totals.addLines(FCollection.fold(
        accumulateCodeLines(), 0,
        this.coverage.getClassInfo(mutationMetaData.getMutatedClass())));
    totals.addLinesCovered(this.coverage.getNumberOfCoveredLines(mutationMetaData
        .getMutatedClass()));
    totals.addMutations(mutationMetaData.getNumberOfMutations());
    totals.addMutationsDetetcted(mutationMetaData.getNumberOfDetetectedMutations());
    
   
    final MutationTestSummaryData summaryData = new MutationTestSummaryData(
        mutationMetaData.getFirstFileName(),
        mutationMetaData.getMutatedClass(), mutationMetaData.getTestClasses(), totals);
    String packageName = getPackageName(mutationMetaData);
    
    packageSummaryData.add(packageName, summaryData);    

  }



  private String getPackageName(MutationMetaData mutationMetaData) {
    String fileName = mutationMetaData.getMutatedClass().iterator().next();
    int lastDot = fileName.lastIndexOf('.');
    return lastDot > 0 ? fileName.substring(0, lastDot) : "default";
  }

  private F2<Integer, ClassInfo, Integer> accumulateCodeLines() {
    return new F2<Integer, ClassInfo, Integer>() {

      public Integer apply(final Integer a, final ClassInfo b) {
        return a + b.getNumberOfCodeLines();
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
    return FCollection.map(classes, classInfoToJavaName());
  }

  private F<ClassInfo, String> classInfoToJavaName() {
    return new F<ClassInfo, String>() {

      public String apply(final ClassInfo a) {
        return a.getName().asJavaName();
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
    createIndexPage();
  }

  private void createIndexPage() {

    final StringTemplateGroup group = new StringTemplateGroup("mutation_test");
    final StringTemplate st = group
        .getInstanceOf("templates/mutation/mutation_package_index");

    final Writer writer = this.outputStrategy.createWriterForFile("index.html");
    MutationTotals totals = new MutationTotals();
    for (PackageSummaryData psData : packageSummaryData.values()) {
      totals.add(psData.getTotals());
      createPackageIndexPage(psData);
    }

    st.setAttribute("totals", totals);
    st.setAttribute("packageSummaries", packageSummaryData.values());
    try {
      writer.write(st.toString());
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void createPackageIndexPage(PackageSummaryData psData) {
    final StringTemplateGroup group = new StringTemplateGroup("mutation_test");
    final StringTemplate st = group
        .getInstanceOf("templates/mutation/mutation_class_index");

    final Writer writer = this.outputStrategy.createWriterForFile(psData
        .getPackageDirectory() + File.separator + "index.html");
    Collections.sort(psData.getSummaryData());
    st.setAttribute("packageData", psData);
    try {
      writer.write(st.toString());
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public void onRunStart() {

  }

}