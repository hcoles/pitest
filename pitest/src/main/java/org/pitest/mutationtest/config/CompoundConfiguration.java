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
package org.pitest.mutationtest.config;

import java.util.List;

import org.pitest.extension.common.CompoundTestSuiteFinder;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.help.PitHelpError;
import org.pitest.junit.CompoundTestUnitFinder;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestClassIdentifier;
import org.pitest.testapi.TestSuiteFinder;
import org.pitest.testapi.TestUnitFinder;

public class CompoundConfiguration implements Configuration {

  private final Iterable<Configuration>     configs;
  private final CompoundTestUnitFinder      testUnitFinder;
  private final CompoundTestSuiteFinder     suiteFinder;
  private final CompoundTestClassIdentifier testIdentifier;

  public CompoundConfiguration(final Iterable<Configuration> configs) {
    this.configs = configs;
    this.testUnitFinder = new CompoundTestUnitFinder(FCollection.map(configs,
        asTestUnitFinders()));
    this.suiteFinder = new CompoundTestSuiteFinder(FCollection.map(configs,
        asSuiteFinders()));
    this.testIdentifier = new CompoundTestClassIdentifier(FCollection.map(
        configs, asTestIdentifier()));
  }

  private static F<Configuration, TestClassIdentifier> asTestIdentifier() {

    return new F<Configuration, TestClassIdentifier>() {
      @Override
      public TestClassIdentifier apply(final Configuration a) {
        return a.testClassIdentifier();
      }

    };

  }

  private static F<Configuration, TestSuiteFinder> asSuiteFinders() {
    return new F<Configuration, TestSuiteFinder>() {
      @Override
      public TestSuiteFinder apply(final Configuration a) {
        return a.testSuiteFinder();
      }

    };
  }

  private static F<Configuration, TestUnitFinder> asTestUnitFinders() {
    return new F<Configuration, TestUnitFinder>() {
      @Override
      public TestUnitFinder apply(final Configuration a) {
        return a.testUnitFinder();
      }

    };
  }

  @Override
  public TestUnitFinder testUnitFinder() {
    return this.testUnitFinder;
  }

  @Override
  public TestSuiteFinder testSuiteFinder() {
    return this.suiteFinder;
  }

  @Override
  public TestClassIdentifier testClassIdentifier() {
    return this.testIdentifier;
  }

  @Override
  public Option<PitHelpError> verifyEnvironment() {
    final List<PitHelpError> verificationResults = FCollection.flatMap(
        this.configs, verify());
    if (verificationResults.isEmpty()) {
      return Option.none();
    }

    return Option.some(verificationResults.iterator().next());
  }

  private static F<Configuration, Iterable<PitHelpError>> verify() {
    return new F<Configuration, Iterable<PitHelpError>>() {
      @Override
      public Iterable<PitHelpError> apply(final Configuration a) {
        return a.verifyEnvironment();
      }

    };
  }

}
