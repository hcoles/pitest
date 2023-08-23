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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.pitest.coverage.ClassLines;
import org.pitest.coverage.TestInfo;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.util.Log;

public class MutationTestSummaryData {

  private final String                     fileName;
  private final Set<String>                mutators  = new TreeSet<>();
  private final Map<MutationIdentifier,MutationResult> mutations = new HashMap<>();
  private final Set<ClassLines>             classes   = new HashSet<>();

  private long                             numberOfCoveredLines;

  public MutationTestSummaryData(final String fileName,
      final Collection<MutationResult> results,
      final Collection<String> mutators, final Collection<ClassLines> classes,
      final long numberOfCoveredLines) {
    this.fileName = fileName;
    this.mutations.putAll(resultsToMap(results));
    this.mutators.addAll(mutators);
    this.classes.addAll(classes);
    this.numberOfCoveredLines = numberOfCoveredLines;
  }

  public MutationTotals getTotals() {
    final MutationTotals mt = new MutationTotals();
    mt.addFiles(1);
    mt.addMutations(this.getNumberOfMutations());
    mt.addMutationsDetetcted(this.getNumberOfMutationsDetected());
    mt.addLines(getNumberOfLines());
    mt.addLinesCovered(this.numberOfCoveredLines);
    mt.addMutationsWithCoverage(this.getNumberOfMutationsWithCoverage());
    return mt;
  }

  private long getNumberOfMutationsWithCoverage() {
    return this.mutations.values().stream()
            .filter(it -> it.getStatus().hasCoverage())
            .count();
  }

  public String getPackageName() {
    Iterator<ClassLines> iterator = getMutatedClasses().iterator();
    if (!iterator.hasNext()) {
      Log.getLogger().log(Level.WARNING, "Can't get package name for " + fileName + "."
        + "There is no mutated classes. It may happen if you are using report-aggregate "
        + "goal to merge reports using a dedicated maven project "
        + "and the dependency that contains the mutated code is missing");
      return "default";
    }
    final String packageName = iterator.next().name()
      .asJavaName();
    final int lastDot = packageName.lastIndexOf('.');
    return lastDot > 0 ? packageName.substring(0, lastDot) : "default";
  }

  public void add(final MutationTestSummaryData data) {
    // FIXME, would need to replace here instead of add
    this.mutations.putAll(data.mutations);
    this.mutators.addAll(data.getMutators());
    final int classesBefore = this.classes.size();
    this.classes.addAll(data.classes);
    if (classesBefore < this.classes.size()) {
      this.numberOfCoveredLines += data.numberOfCoveredLines;
    }
  }

  public Collection<TestInfo> getTests() {
    return this.mutations.values().stream()
            .flatMap(a -> a.getDetails().getTestsInOrder().stream())
            .distinct()
            .collect(Collectors.toList());
  }

  public String getFileName() {
    return this.fileName;
  }

  public Collection<ClassLines> getMutatedClasses() {
    return this.classes;
  }

  public Set<String> getMutators() {
    return this.mutators;
  }

  public MutationResultList getResults() {
    return new MutationResultList(this.mutations.values());
  }

  public Collection<ClassLines> getClasses() {
    return this.classes;
  }

  private int getNumberOfLines() {
    return FCollection.fold(accumulateCodeLines(), 0, this.classes);
  }

  private BiFunction<Integer, ClassLines, Integer> accumulateCodeLines() {
    return (a, b) -> a + b.getNumberOfCodeLines();
  }

  private long getNumberOfMutations() {
    return this.mutations.size();
  }

  private long getNumberOfMutationsDetected() {
    int count = 0;
    for (final MutationResult each : this.mutations.values()) {
      if (each.getStatus().isDetected()) {
        count++;
      }
    }
    return count;
  }

  private static Map<MutationIdentifier, MutationResult> resultsToMap(Collection<MutationResult> results) {
    return results.stream().collect(Collectors.toMap(mr -> mr.getDetails().getId(), Function.identity()));
  }


}
