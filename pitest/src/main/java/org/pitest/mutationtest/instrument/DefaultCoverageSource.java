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

package org.pitest.mutationtest.instrument;

import static org.pitest.functional.Prelude.putToMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.pitest.Description;
import org.pitest.Pitest;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestFilter;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.NullDiscoveryListener;
import org.pitest.extension.common.UnGroupedStrategy;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.util.Functions;
import org.pitest.util.MemoryEfficientHashMap;

public class DefaultCoverageSource implements CoverageSource {

  private final Map<String, Long>                timings;
  private final Map<ClassLine, Set<Description>> coverageByTestUnit;
  protected final Configuration                  pitConfig;
  protected final Collection<String>             testClasses = new ArrayList<String>();

  public DefaultCoverageSource(final Collection<String> tests,
      final Configuration pitConfig, final Map<String, Long> timings,
      final Map<ClassLine, Set<Description>> coverageByTestUnit) {
    this.testClasses.addAll(tests);

    this.pitConfig = pitConfig;
    this.timings = timings;
    this.coverageByTestUnit = coverageByTestUnit;
  }

  public Option<Statistics> getStatistics(final List<TestUnit> tests,
      final Collection<String> classesToMutate) {
    final Statistics stats = new Statistics(true, matchTestsToTimes(tests),
        relevantCoverage(tests, classesToMutate));
    return Option.some(stats);
  }

  public List<TestUnit> getTests(final ClassLoader loader) {
    return findTestUnits(loader);
  }

  protected List<TestUnit> findTestUnits(final ClassLoader loader) {
    final Collection<Class<?>> tcs = FCollection.flatMap(this.testClasses,
        Functions.stringToClass(loader));
    // FIXME we do not apply any test filters. Is this what the user
    // expects?
    return Pitest.findTestUnitsForAllSuppliedClasses(this.pitConfig,
        new NullDiscoveryListener(), new UnGroupedStrategy(),
        Option.<TestFilter> none(), tcs.toArray(new Class<?>[tcs.size()]));
  }

  private Map<ClassLine, List<TestUnit>> relevantCoverage(
      final List<TestUnit> tests, final Collection<String> classesToMutate) {
    final Map<ClassLine, List<TestUnit>> result = new MemoryEfficientHashMap<ClassLine, List<TestUnit>>();
    for (final Entry<ClassLine, Set<Description>> each : this.coverageByTestUnit
        .entrySet()) {

      if (classesToMutate.contains(each.getKey().clazz.replace("/", "."))) {
        result.put(each.getKey(),
            matchDescriptionaToTestUnit(each.getValue(), tests));
      }
    }

    return result;

  }

  private List<TestUnit> matchDescriptionaToTestUnit(
      final Set<Description> descriptions, final List<TestUnit> tests) {

    return FCollection.filter(tests, matchesOneOf(descriptions));

  }

  private F<TestUnit, Boolean> matchesOneOf(final Set<Description> descriptions) {
    return new F<TestUnit, Boolean>() {

      public Boolean apply(final TestUnit a) {

        final boolean match = descriptions.contains(a.getDescription());
        return match;

      }

    };
  }

  private Map<TestUnit, Long> matchTestsToTimes(final List<TestUnit> tests) {
    final Map<TestUnit, Long> map = new MemoryEfficientHashMap<TestUnit, Long>();
    FCollection.forEach(tests, putToMap(map, testUnitToTime()));
    return map;
  }

  private F<TestUnit, Long> testUnitToTime() {
    return new F<TestUnit, Long>() {
      public Long apply(final TestUnit a) {
        return DefaultCoverageSource.this.timings.get(a.getDescription()
            .toString());
      }

    };
  }

}
