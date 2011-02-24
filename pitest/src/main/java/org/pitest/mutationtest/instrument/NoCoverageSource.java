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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pitest.Pitest;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestFilter;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.NullDiscoveryListener;
import org.pitest.extension.common.UnGroupedStrategy;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.util.Functions;

public class NoCoverageSource implements CoverageSource {

  private final Configuration      pitConfig;
  private final Collection<String> testClasses = new ArrayList<String>();

  public NoCoverageSource(final Collection<String> tests,
      final Configuration pitConfig) {
    this.testClasses.addAll(tests);

    this.pitConfig = pitConfig;
  }

  public Option<Statistics> getStatistics(final List<TestUnit> tests,
      final Collection<String> classesToMutate) {
    return Option.none();
  }

  public List<TestUnit> getTests(final ClassLoader loader) {
    return findTestUnits(loader);
  }

  private List<TestUnit> findTestUnits(final ClassLoader loader) {
    final Collection<Class<?>> tcs = FCollection.flatMap(this.testClasses,
        Functions.stringToClass(loader));
    // FIXME we do not apply any test filters. Is this what the user
    // expects?
    return Pitest.findTestUnitsForAllSuppliedClasses(this.pitConfig,
        new NullDiscoveryListener(), new UnGroupedStrategy(),
        Option.<TestFilter> none(), tcs.toArray(new Class<?>[tcs.size()]));
  }

}
