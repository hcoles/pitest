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
package org.pitest.internal;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.pitest.ConcreteConfiguration;
import org.pitest.extension.Configuration;
import org.pitest.extension.InstantiationStrategy;
import org.pitest.extension.TestUnitFinder;
import org.pitest.extension.TestUnitProcessor;
import org.pitest.functional.Option;

public class ConfigParser {

  private final Class<?> clazz;

  public ConfigParser(final Class<?> clazz) {
    this.clazz = clazz;
  }

  public Configuration create(final Configuration baseConfig) {

    if (baseConfig.allowConfigurationChange()) {

      final List<InstantiationStrategy> is = determineInstantiationStrategy(
          this.clazz).getOrElse(baseConfig.instantiationStrategies());

      final Collection<TestUnitProcessor> visitors = determineTestUnitProcessors(
          this.clazz).getOrElse(baseConfig.testUnitProcessors());

      final Set<TestUnitFinder> finders = baseConfig.testUnitFinders();
      // final Set<TestUnitFinder> finders = determineTestUnitFinder(this.clazz)
      // .getOrElse(baseConfig.testUnitFinders());

      return new ConcreteConfiguration(baseConfig.allowConfigurationChange(),
          is, visitors, finders, baseConfig.testSuiteFinders());
    } else {
      return baseConfig;
    }

  }

  private Option<TestUnitFinder> determineTestUnitFinder(final Class<?> clazz2) {
    return Option.none();
  }

  private Option<List<InstantiationStrategy>> determineInstantiationStrategy(
      final Class<?> clazz) {
    return Option.none();
  }

  private Option<Collection<TestUnitProcessor>> determineTestUnitProcessors(
      final Class<?> clazz) {
    return Option.none();
  }

}
