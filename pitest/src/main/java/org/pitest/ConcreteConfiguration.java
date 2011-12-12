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
import org.pitest.extension.TestClassIdentifier;
import org.pitest.extension.TestSuiteFinder;
import org.pitest.extension.TestUnitFinder;
import org.pitest.extension.common.NoTestClassIdentifier;
import org.pitest.extension.common.NoTestFinder;
import org.pitest.extension.common.NoTestSuiteFinder;
import org.pitest.junit.CompoundTestUnitFinder;

/**
 * @author henry
 * 
 */
public final class ConcreteConfiguration implements Configuration {

  private TestUnitFinder  testUnitFinder  = new NoTestFinder();
  private TestSuiteFinder testSuiteFinder = new NoTestSuiteFinder();
  private  TestClassIdentifier testClassIdentifier = new NoTestClassIdentifier();

  public ConcreteConfiguration() {
  }

  public ConcreteConfiguration(final Configuration configuration) {
    addConfiguration(configuration);
  }

  public TestUnitFinder testUnitFinder() {
    return this.testUnitFinder;
  }

  public TestSuiteFinder testSuiteFinder() {
    return this.testSuiteFinder;
  }

  public void addConfiguration(final Configuration configuration) {

    this.testUnitFinder = new CompoundTestUnitFinder(Arrays.asList(
        this.testUnitFinder, configuration.testUnitFinder()));

    this.testSuiteFinder = new CompoundTestSuiteFinder(Arrays.asList(
        this.testSuiteFinder, configuration.testSuiteFinder()));

    this.testClassIdentifier = configuration.testClassIdentifier();

  }

  public TestClassIdentifier testClassIdentifier() {
    return this.testClassIdentifier;
  }

}
