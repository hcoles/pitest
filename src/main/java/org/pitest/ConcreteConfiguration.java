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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.pitest.extension.Configuration;
import org.pitest.extension.ConfigurationUpdater;
import org.pitest.extension.InstantiationStrategy;
import org.pitest.extension.TestSuiteFinder;
import org.pitest.extension.TestUnitFinder;
import org.pitest.extension.TestUnitProcessor;
import org.pitest.internal.TestClass;

/**
 * @author henry
 * 
 */
public final class ConcreteConfiguration implements Configuration {

  private final boolean                          allowConfigurationChange;
  private final List<InstantiationStrategy>      instantiationStrategy = new ArrayList<InstantiationStrategy>();
  private final List<TestUnitProcessor>          testProcessors        = new ArrayList<TestUnitProcessor>();
  private final List<TestUnitFinder>             testUnitFinders       = new ArrayList<TestUnitFinder>();
  private final List<TestSuiteFinder>            testSuiteFinders      = new LinkedList<TestSuiteFinder>();
  private final Collection<ConfigurationUpdater> configurationUpdaters = new ArrayList<ConfigurationUpdater>();

  public ConcreteConfiguration(final boolean allowConfigurationChange) {
    this.allowConfigurationChange = allowConfigurationChange;
  }

  public ConcreteConfiguration(final Configuration configuration) {
    this(configuration.allowConfigurationChange());
    addConfiguration(configuration);
  }

  public List<TestUnitFinder> testUnitFinders() {
    return this.testUnitFinders;
  }

  public List<TestUnitProcessor> testUnitProcessors() {
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

  public Collection<ConfigurationUpdater> configurationUpdaters() {
    return this.configurationUpdaters;
  }

  public static Configuration updateConfig(final Configuration startConfig,
      final TestClass tc) {
    if (!startConfig.allowConfigurationChange()) {
      return startConfig;
    }
    Configuration newConfig = startConfig;
    for (final ConfigurationUpdater each : startConfig.configurationUpdaters()) {
      newConfig = each.updateConfiguration(tc.getClazz(), newConfig);
    }

    return newConfig;
  }

  public void addConfiguration(final Configuration configuration) {
    this.testUnitFinders.addAll(configuration.testUnitFinders());
    this.instantiationStrategy.addAll(configuration.instantiationStrategies());
    this.testProcessors.addAll(configuration.testUnitProcessors());
    this.testSuiteFinders.addAll(configuration.testSuiteFinders());
    this.configurationUpdaters.addAll(configuration.configurationUpdaters());
  }

}
