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

import org.pitest.coverage.ClassLines;
import org.pitest.coverage.ReportCoverage;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.MutationResult;
import org.pitest.util.StringUtil;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AnnotatedLineFactory {

  private final Collection<MutationResult> mutations;
  private final Collection<ClassLines> classesInFile;
  private final Set<Integer>  coveredLines;
  private final boolean reportCoverage;

  public AnnotatedLineFactory(
          final Collection<MutationResult> mutations,
          final ReportCoverage coverage,
          final Collection<ClassLines> classes,
          boolean reportCoverage) {
    this.mutations = mutations;
    this.classesInFile = classes;
    this.coveredLines = findCoveredLines(classes,coverage);
    this.reportCoverage = reportCoverage;
  }

  private Set<Integer> findCoveredLines(Collection<ClassLines> classes, ReportCoverage coverage) {
   return classes.stream()
            .flatMap(cl -> coverage.getCoveredLines(cl.name()).stream().map(l -> l.getLineNumber()))
            .collect(Collectors.toSet());
  }

  public List<Line> convert(final Reader source) throws IOException {
    try {
      final InputStreamLineIterable lines = new InputStreamLineIterable(source);
      return StreamSupport.stream(lines.spliterator(), false)
              .map(stringToAnnotatedLine())
              .collect(Collectors.toList());
    } finally {
      source.close();
    }

  }

  private Function<String, Line> stringToAnnotatedLine() {
    return new Function<String, Line>() {
      private int lineNumber = 1;

      @Override
      public Line apply(final String a) {
        final Line l = new Line(this.lineNumber,
            StringUtil.escapeBasicHtmlChars(a), lineCovered(this.lineNumber),
            getMutationsForLine(this.lineNumber));
        this.lineNumber++;
        return l;
      }

    };
  }

  private List<MutationResult> getMutationsForLine(final int lineNumber) {
    return this.mutations.stream()
        .filter(isAtLineNumber(lineNumber))
        .collect(Collectors.toList());
  }

  private Predicate<MutationResult> isAtLineNumber(final int lineNumber) {
    return result -> result.getDetails().getLineNumber() == lineNumber;
  }

  private LineStatus lineCovered(final int line) {

    if (!isCodeLine(line) || !reportCoverage) {
      return LineStatus.NotApplicable;
    } else {
      if (isLineCovered(line)) {
        return LineStatus.Covered;
      } else {
        return LineStatus.NotCovered;
      }
    }
  }

  private boolean isCodeLine(final int line) {
    return FCollection.contains(this.classesInFile, a -> a.isCodeLine(line));
  }

  private boolean isLineCovered(final int line) {
    return coveredLines.contains(line);
  }

}
