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

import java.util.List;
import java.util.logging.Logger;

import org.pitest.extension.TestUnit;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.util.Log;

public class TimeOutDecoratedTestSource {

  private final static Logger         LOG = Log.getLogger();

  private final Statistics            stats;
  private final List<TestUnit>        allTests;
  private final TimeoutLengthStrategy timeoutStrategy;

  public TimeOutDecoratedTestSource(final Statistics stats,
      final TimeoutLengthStrategy timeoutStrategy, final List<TestUnit> allTests) {
    this.stats = stats;
    this.allTests = allTests;
    this.timeoutStrategy = timeoutStrategy;
  }

  public List<TestUnit> pickTests(final Mutant m) {
    if (this.stats.hasCoverageData() && !hasMutationInStaticInitializer(m)) {
      return decorateForTimeouts(
          this.stats.getTestForLineNumber(m.getDetails().getClassLine()),
          this.stats);
    } else {
      LOG.warning("Using untargetted tests");
      return decorateForTimeouts(this.allTests, this.stats);
    }
  }

  private List<TestUnit> decorateForTimeouts(final List<TestUnit> tests,
      final Statistics stats) {
    return FCollection.map(tests, decorateTestForTimeout(stats));
  }

  private F<TestUnit, TestUnit> decorateTestForTimeout(final Statistics stats) {
    return new F<TestUnit, TestUnit>() {

      public TestUnit apply(final TestUnit tu) {
        return new MutationTimeoutDecorator(tu,
            TimeOutDecoratedTestSource.this.timeoutStrategy,
            stats.getExecutionTime(tu));
      }

    };
  }

  private boolean hasMutationInStaticInitializer(final Mutant mutant) {
    return (mutant.getDetails().getId().isMutated())
        && mutant.getDetails().isInStaticInitializer();
  }

}
