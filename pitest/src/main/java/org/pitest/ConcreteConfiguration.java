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

import java.util.Arrays;

import org.pitest.extension.Configuration;
import org.pitest.extension.ConfigurationUpdater;
import org.pitest.extension.StaticConfigUpdater;
import org.pitest.extension.TestSuiteFinder;
import org.pitest.extension.TestUnitFinder;
import org.pitest.extension.TestUnitProcessor;
import org.pitest.extension.common.IdentityTestUnitProcessor;
import org.pitest.extension.common.NoTestFinder;
import org.pitest.extension.common.NoTestSuiteFinder;
import org.pitest.extension.common.NullConfigurationUpdater;
import org.pitest.extension.common.NullStaticConfigUpdater;
import org.pitest.internal.TestClass;
import org.pitest.junit.CompoundConfigurationUpdater;
import org.pitest.junit.CompoundTestUnitFinder;

/**
 * @author henry
 * 
 */
public final class ConcreteConfiguration implements Configuration {

  // private final boolean allowConfigurationChange;
  private TestUnitProcessor    testProcessor              = new IdentityTestUnitProcessor();
  private TestUnitFinder       testUnitFinder             = new NoTestFinder();
  private TestUnitFinder       mutationTestFinder         = new NoTestFinder();
  private TestSuiteFinder      testSuiteFinder            = new NoTestSuiteFinder();
  private ConfigurationUpdater configurationUpdater       = new NullConfigurationUpdater();
  private StaticConfigUpdater  staticConfigurationUpdater = new NullStaticConfigUpdater();

  public ConcreteConfiguration() {
  }

  public ConcreteConfiguration(final Configuration configuration) {
    addConfiguration(configuration);
  }

  public TestUnitFinder testUnitFinder() {
    return this.testUnitFinder;
  }

  public TestUnitProcessor testUnitProcessor() {
    return this.testProcessor;
  }

  public TestSuiteFinder testSuiteFinder() {
    return this.testSuiteFinder;
  }

  public ConfigurationUpdater configurationUpdater() {
    return this.configurationUpdater;
  }

  public static Configuration updateConfig(final Configuration startConfig,
      final TestClass tc) {
    return startConfig.configurationUpdater().updateConfiguration(
        tc.getClazz(), startConfig);
  }

  public void addConfiguration(final Configuration configuration) {
    this.mutationTestFinder = new CompoundTestUnitFinder(Arrays.asList(
        this.mutationTestFinder, configuration.mutationTestFinder()));

    this.testUnitFinder = new CompoundTestUnitFinder(Arrays.asList(
        this.testUnitFinder, configuration.testUnitFinder()));

    // this.instantiationStrategy.addAll(configuration.instantiationStrategies());
    this.testProcessor = new CompoundTestUnitProcessor(Arrays.asList(
        this.testProcessor, configuration.testUnitProcessor()));
    this.testSuiteFinder = new CompoundTestSuiteFinder(Arrays.asList(
        this.testSuiteFinder, configuration.testSuiteFinder()));
    this.configurationUpdater = new CompoundConfigurationUpdater(Arrays.asList(
        this.configurationUpdater, configuration.configurationUpdater()));
    this.staticConfigurationUpdater = new CompoundStaticConfigurationUpdater(
        Arrays.asList(this.staticConfigurationUpdater,
            configuration.staticConfigurationUpdater()));

  }

  public StaticConfigUpdater staticConfigurationUpdater() {
    return this.staticConfigurationUpdater;
  }

  public TestUnitFinder mutationTestFinder() {
    return this.mutationTestFinder;
  }

  public void setMutationTestFinder(final TestUnitFinder mtf) {
    this.mutationTestFinder = mtf;
  }

}
