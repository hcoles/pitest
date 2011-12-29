/*
 * Copyright 2011 Henry Coles
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
package org.pitest.extension.common;

import org.pitest.CompoundTestSuiteFinder;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestClassIdentifier;
import org.pitest.extension.TestSuiteFinder;
import org.pitest.extension.TestUnitFinder;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.junit.CompoundTestUnitFinder;

public class CompoundConfiguration implements Configuration {

  private final CompoundTestUnitFinder      testUnitFinder;
  private final CompoundTestSuiteFinder     suiteFinder;
  private final CompoundTestClassIdentifier testIdentifier;

  public CompoundConfiguration(Iterable<Configuration> configs) {
    this.testUnitFinder = new CompoundTestUnitFinder(FCollection.map(configs,
        asTestUnitFinders()));
    this.suiteFinder = new CompoundTestSuiteFinder(FCollection.map(configs,
        asSuiteFinders()));
    this.testIdentifier = new CompoundTestClassIdentifier(FCollection.map(
        configs, asTestIdentifier()));
  }

  private F<Configuration, TestClassIdentifier> asTestIdentifier() {

    return new F<Configuration, TestClassIdentifier>() {
      public TestClassIdentifier apply(Configuration a) {
        return a.testClassIdentifier();
      }

    };

  }

  private F<Configuration, TestSuiteFinder> asSuiteFinders() {
    return new F<Configuration, TestSuiteFinder>() {
      public TestSuiteFinder apply(Configuration a) {
        return a.testSuiteFinder();
      }

    };
  }

  private static F<Configuration, TestUnitFinder> asTestUnitFinders() {
    return new F<Configuration, TestUnitFinder>() {
      public TestUnitFinder apply(Configuration a) {
        return a.testUnitFinder();
      }

    };
  }

  public TestUnitFinder testUnitFinder() {
    return this.testUnitFinder;
  }

  public TestSuiteFinder testSuiteFinder() {
    return this.suiteFinder;
  }

  public TestClassIdentifier testClassIdentifier() {
    return this.testIdentifier;
  }

}
