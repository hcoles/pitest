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
package org.pitest;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.pitest.extension.Configuration;
import org.pitest.extension.InstantiationStrategy;
import org.pitest.extension.TestSuiteFinder;
import org.pitest.extension.TestUnitFinder;
import org.pitest.extension.TestUnitProcessor;

/**
 * @author henry
 * 
 */
public final class ConcreteConfiguration implements Configuration {

  private final boolean                     allowConfigurationChange;
  private final List<InstantiationStrategy> instantiationStrategy;
  private final Set<TestUnitProcessor>      testProcessors   = new LinkedHashSet<TestUnitProcessor>();
  private final Set<TestUnitFinder>         testUnitFinders  = new LinkedHashSet<TestUnitFinder>();
  private final List<TestSuiteFinder>       testSuiteFinders = new LinkedList<TestSuiteFinder>();

  public ConcreteConfiguration(final boolean allowConfigurationChange,
      final List<InstantiationStrategy> instantiationStrategy,
      final Collection<TestUnitProcessor> testVisitors,
      final Collection<TestUnitFinder> testUnitFinders,
      final Collection<TestSuiteFinder> testSuiteFinders) {
    this.allowConfigurationChange = allowConfigurationChange;
    this.testUnitFinders.addAll(testUnitFinders);
    this.instantiationStrategy = instantiationStrategy;
    this.testProcessors.addAll(testVisitors);
    this.testSuiteFinders.addAll(testSuiteFinders);
  }

  public Set<TestUnitFinder> testUnitFinders() {
    return this.testUnitFinders;
  }

  public Set<TestUnitProcessor> testUnitProcessors() {
    return this.testProcessors;
  }

  public boolean allowConfigurationChange() {
    return this.allowConfigurationChange;
  }

  public Collection<TestSuiteFinder> testSuiteFinders() {
    return this.testSuiteFinders;
  }

  public List<InstantiationStrategy> instantiationStrategies() {
    return this.instantiationStrategy;
  }

}
