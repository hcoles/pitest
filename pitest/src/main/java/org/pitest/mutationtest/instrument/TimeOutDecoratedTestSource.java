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
import java.util.Map;

import org.pitest.coverage.domain.TestInfo;
import org.pitest.extension.TestUnit;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.util.MemoryEfficientHashMap;

public class TimeOutDecoratedTestSource {

  private final Map<String, TestUnit> allTests = new MemoryEfficientHashMap<String, TestUnit>();
  private final TimeoutLengthStrategy timeoutStrategy;

  public TimeOutDecoratedTestSource(
      final TimeoutLengthStrategy timeoutStrategy, final List<TestUnit> allTests) {
    this.timeoutStrategy = timeoutStrategy;
    mapTests(allTests);
  }

  private void mapTests(final List<TestUnit> tests) {
    for (final TestUnit each : tests) {
      this.allTests.put(each.getDescription().toString(), each);
    }
  }

  public List<TestUnit> translateTests(final List<TestInfo> testsInOrder) {
    return FCollection.map(testsInOrder, testToTestUnit());
  }

  private F<TestInfo, TestUnit> testToTestUnit() {
    return new F<TestInfo, TestUnit>() {

      public TestUnit apply(final TestInfo a) {

        return new MutationTimeoutDecorator(
            TimeOutDecoratedTestSource.this.allTests.get(a.getName()),
            TimeOutDecoratedTestSource.this.timeoutStrategy, a.getTime());
      }

    };
  }

}
