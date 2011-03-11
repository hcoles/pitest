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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.pitest.extension.Configuration;
import org.pitest.extension.ConfigurationUpdater;
import org.pitest.extension.InstantiationStrategy;
import org.pitest.extension.MethodFinder;
import org.pitest.extension.StaticConfigUpdater;
import org.pitest.extension.TestSuiteFinder;
import org.pitest.extension.TestUnitFinder;
import org.pitest.extension.TestUnitProcessor;
import org.pitest.extension.common.BasicTestUnitFinder;
import org.pitest.extension.common.DefaultConfigurationUpdater;
import org.pitest.extension.common.DefaultStaticConfigUpdater;
import org.pitest.extension.common.IgnoreTestProcessor;
import org.pitest.extension.common.NamedTestSingleStringConstructorInstantiationStrategy;
import org.pitest.extension.common.NoArgsConstructorInstantiationStrategy;
import org.pitest.extension.common.SimpleAnnotationTestMethodFinder;
import org.pitest.extension.common.testsuitefinder.ClasspathSuiteFinder;
import org.pitest.extension.common.testsuitefinder.PITStaticMethodSuiteFinder;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.MutationSuiteConfigUpdater;

public class JUnitCompatibleConfiguration implements Configuration {

  public List<TestUnitProcessor> testUnitProcessors() {
    final List<TestUnitProcessor> tups = new ArrayList<TestUnitProcessor>();
    tups.add(new IgnoreTestProcessor(org.junit.Ignore.class));
    tups.add(new TimeoutProcessor());
    return tups;
  }

  public List<TestUnitFinder> testUnitFinders() {
    final Set<MethodFinder> beforeClassFinders = Collections
        .<MethodFinder> singleton(new SimpleAnnotationTestMethodFinder(
            org.junit.BeforeClass.class));

    final Set<MethodFinder> afterClassFinders = Collections
        .<MethodFinder> singleton(new SimpleAnnotationTestMethodFinder(
            org.junit.AfterClass.class));

    final LinkedHashSet<MethodFinder> beforeMethodFinders = new LinkedHashSet<MethodFinder>();
    beforeMethodFinders.add(new SimpleAnnotationTestMethodFinder(
        org.junit.Before.class));
    beforeMethodFinders.add(new JUnit3NameBasedMethodFinder("setUp"));

    final Set<MethodFinder> afterMethodFinders = new LinkedHashSet<MethodFinder>();
    afterMethodFinders.add(new SimpleAnnotationTestMethodFinder(
        org.junit.After.class));
    afterMethodFinders.add(new JUnit3NameBasedMethodFinder("tearDown"));

    final Set<MethodFinder> tmfs = new LinkedHashSet<MethodFinder>();

    tmfs.add(JUnit4TestMethodFinder.instance());
    tmfs.add(JUnit3TestMethodFinder.instance());

    final List<TestUnitFinder> tus = new ArrayList<TestUnitFinder>();

    tus.add(new BasicTestUnitFinder(excludeSpecialCases(), tmfs,
        beforeMethodFinders, afterMethodFinders, beforeClassFinders,
        afterClassFinders));

    tus.add(new JUnitCustomRunnerTestUnitFinder());
    tus.add(new CustomJUnit3TestUnitFinder());

    return tus;
  }

  private Predicate<Class<?>> excludeSpecialCases() {

    return new Predicate<Class<?>>() {

      public Boolean apply(final Class<?> a) {
        return !CustomJUnit3TestUnitFinder.isCustomJUnit3Class(a);
      }

    };

  }

  public boolean allowConfigurationChange() {
    return true;
  }

  public Collection<TestSuiteFinder> testSuiteFinders() {
    return Arrays.<TestSuiteFinder> asList(new PITStaticMethodSuiteFinder(),
        new JUnit4SuiteFinder(), new ClasspathSuiteFinder(),
        new RunnerSuiteFinder());
  }

  public List<InstantiationStrategy> instantiationStrategies() {
    // order is important
    return Arrays.<InstantiationStrategy> asList(
        new ParameterizedInstantiationStrategy(),
        new NamedTestSingleStringConstructorInstantiationStrategy(),
        new NoArgsConstructorInstantiationStrategy());
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

}
