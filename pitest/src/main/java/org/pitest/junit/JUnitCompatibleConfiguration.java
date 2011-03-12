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
package org.pitest.junit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.pitest.extension.Configuration;
import org.pitest.extension.ConfigurationUpdater;
import org.pitest.extension.StaticConfigUpdater;
import org.pitest.extension.TestSuiteFinder;
import org.pitest.extension.TestUnitFinder;
import org.pitest.extension.TestUnitProcessor;
import org.pitest.extension.common.DefaultConfigurationUpdater;
import org.pitest.extension.common.DefaultStaticConfigUpdater;
import org.pitest.extension.common.IgnoreTestProcessor;
import org.pitest.extension.common.NoTestFinder;
import org.pitest.extension.common.testsuitefinder.PITStaticMethodSuiteFinder;
import org.pitest.mutationtest.MutationSuiteConfigUpdater;

public class JUnitCompatibleConfiguration implements Configuration {

  public List<TestUnitProcessor> testUnitProcessors() {
    final List<TestUnitProcessor> tups = new ArrayList<TestUnitProcessor>();
    tups.add(new IgnoreTestProcessor(org.junit.Ignore.class));
    tups.add(new TimeoutProcessor());
    return tups;
  }

  public TestUnitFinder testUnitFinder() {

    return new CombinedJUnitTestFinder();
  }

  public boolean allowConfigurationChange() {
    return true;
  }

  public Collection<TestSuiteFinder> testSuiteFinders() {
    return Arrays.<TestSuiteFinder> asList(new PITStaticMethodSuiteFinder(),
        new JUnit4SuiteFinder(), new RunnerSuiteFinder());
  }

  public Collection<ConfigurationUpdater> configurationUpdaters() {
    return Arrays.<ConfigurationUpdater> asList(
        MutationSuiteConfigUpdater.instance(),
        new DefaultConfigurationUpdater());
  }

  public Collection<StaticConfigUpdater> staticConfigurationUpdaters() {
    return Collections
        .<StaticConfigUpdater> singletonList(new DefaultStaticConfigUpdater());
  }

  public TestUnitFinder mutationTestFinder() {
    return new NoTestFinder();
  }

}
