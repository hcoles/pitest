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

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.pitest.Description;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.util.Log;

public class DefaultCoverageSource implements CoverageSource {

  private final static Logger                    LOG = Log.getLogger();

  private final Map<String, Long>                timings;
  private final Map<ClassLine, Set<Description>> coverageByTestUnit;

  public DefaultCoverageSource(final Map<String, Long> timings,
      final Map<ClassLine, Set<Description>> coverageByTestUnit) {

    this.timings = timings;
    this.coverageByTestUnit = coverageByTestUnit;
  }

  public Collection<TestInfo> getTestsForMutant(final MutationDetails mutation) {
    if (!mutation.isInStaticInitializer()) {
      final Set<Description> tests = this.coverageByTestUnit.get(mutation
          .getClassLine());
      final Set<TestInfo> testInfos = new TreeSet<TestInfo>(timeComparator());
      FCollection.map(tests, descriptionToTestInfo(), testInfos);
      return testInfos;
    } else {
      LOG.warning("Using untargeted tests");

      return FCollection.map(this.timings.entrySet(), entryToTestInfo());

    }

  }

  private Comparator<TestInfo> timeComparator() {
    return new Comparator<TestInfo>() {

      public int compare(final TestInfo arg0, final TestInfo arg1) {
        final Long t0 = arg0.getTime();
        final Long t1 = arg1.getTime();
        return t0.compareTo(t1);
      }

    };
  }

  private F<Entry<String, Long>, TestInfo> entryToTestInfo() {
    return new F<Entry<String, Long>, TestInfo>() {

      public TestInfo apply(final Entry<String, Long> a) {
        return new TestInfo(a.getKey(), a.getValue());
      }

    };
  }

  private F<Description, TestInfo> descriptionToTestInfo() {
    return new F<Description, TestInfo>() {

      public TestInfo apply(final Description a) {
        final long time = DefaultCoverageSource.this.timings.get(a.toString());
        return new TestInfo(a.toString(), time);
      }

    };
  }

}
