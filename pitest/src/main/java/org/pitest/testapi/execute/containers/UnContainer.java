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

package org.pitest.testapi.execute.containers;

import java.util.ArrayList;
import java.util.List;

import org.pitest.testapi.TestResult;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.execute.Container;

public class UnContainer implements Container {

  @Override
  public List<TestResult> execute(final TestUnit group) {
    final List<TestResult> results = new ArrayList<>(12);
    final ConcreteResultCollector rc = new ConcreteResultCollector(results);
    group.execute(rc);
    return results;
  }

}
