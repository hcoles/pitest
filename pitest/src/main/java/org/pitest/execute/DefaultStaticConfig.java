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
package org.pitest.execute;

import java.util.ArrayList;
import java.util.Collection;

import org.pitest.testapi.GroupingStrategy;
import org.pitest.testapi.TestListener;

public class DefaultStaticConfig implements StaticConfiguration {

  private final ResultClassifier         classifier;
  private final GroupingStrategy         groupingStrategy;
  private final Collection<TestListener> testListeners = new ArrayList<TestListener>();

  private DefaultStaticConfig(final ResultClassifier classifier,
      final GroupingStrategy groupStrategy) {
    this.classifier = classifier;
    this.groupingStrategy = groupStrategy;
  }

  public DefaultStaticConfig(final GroupingStrategy groupStrategy) {
    this(new DefaultResultClassifier(), groupStrategy);
  }

  public DefaultStaticConfig() {
    this(new DefaultResultClassifier(), new GroupPerClassStrategy());
  }

  public Collection<TestListener> getTestListeners() {
    return this.testListeners;
  }

  public ResultClassifier getClassifier() {
    return this.classifier;
  }

  public final void addTestListener(final TestListener listener) {
    this.testListeners.add(listener);
  }

  public GroupingStrategy getGroupingStrategy() {
    return this.groupingStrategy;
  }

}
