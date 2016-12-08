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

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

import org.pitest.classinfo.ClassInfo;
import org.pitest.coverage.ClassLine;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalIterable;
import org.pitest.functional.FunctionalList;
import org.pitest.mutationtest.MutationResult;
import org.pitest.util.StringUtil;

public class AnnotatedLineFactory {

  private final FunctionalIterable<MutationResult> mutations;
  private final CoverageDatabase                   statistics;
  private final Collection<ClassInfo>              classesInFile;

  public AnnotatedLineFactory(
      final FunctionalIterable<MutationResult> mutations,
      final CoverageDatabase statistics, final Collection<ClassInfo> classes) {
    this.mutations = mutations;
    this.statistics = statistics;
    this.classesInFile = classes;
  }

  public FunctionalList<Line> convert(final Reader source) throws IOException {
    try {
      final InputStreamLineIterable lines = new InputStreamLineIterable(source);
      return lines.map(stringToAnnotatedLine());
    } finally {
      source.close();
    }

  }

  private F<String, Line> stringToAnnotatedLine() {
    return new F<String, Line>() {
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
    return this.mutations.filter(isAtLineNumber(lineNumber));
  }

  private F<MutationResult, Boolean> isAtLineNumber(final int lineNumber) {
    return new F<MutationResult, Boolean>() {

      @Override
      public Boolean apply(final MutationResult result) {
        return result.getDetails().getLineNumber() == lineNumber;
      }

    };
  }

  private LineStatus lineCovered(final int line) {

    if (!isCodeLine(line)) {
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
    final F<ClassInfo, Boolean> predicate = new F<ClassInfo, Boolean>() {
      @Override
      public Boolean apply(final ClassInfo a) {
        return a.isCodeLine(line);
      }
    };
    return FCollection.contains(this.classesInFile, predicate);
  }

  private boolean isLineCovered(final int line) {
    final F<ClassInfo, Boolean> predicate = new F<ClassInfo, Boolean>() {
      @Override
      public Boolean apply(final ClassInfo a) {
        return !AnnotatedLineFactory.this.statistics.getTestsForClassLine(
            new ClassLine(a.getName().asInternalName(), line)).isEmpty();
      }
    };
    return FCollection.contains(this.classesInFile, predicate);

  }

}
