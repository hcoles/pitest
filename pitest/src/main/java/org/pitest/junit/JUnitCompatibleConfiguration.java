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

import java.util.Arrays;
import java.util.Collection;

import org.pitest.extension.common.CompoundTestSuiteFinder;
import java.util.Optional;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestSuiteFinder;
import org.pitest.testapi.TestUnitFinder;
import org.pitest.util.Preconditions;

public class JUnitCompatibleConfiguration implements Configuration {

  private final TestGroupConfig config;
  private final Collection<String> excludedRunners;
  private final Collection<String> includedTestMethods;

  private static final JUnitVersion MIN_JUNIT_VERSION = JUnitVersion.parse("4.6");

  public JUnitCompatibleConfiguration(TestGroupConfig config, Collection<String> excludedRunners,
                                      Collection<String> includedTestMethods) {
    Preconditions.checkNotNull(config);
    this.config = config;
    this.excludedRunners = excludedRunners;
    this.includedTestMethods = includedTestMethods;
  }

  @Override
  public TestUnitFinder testUnitFinder() {
    return new CompoundTestUnitFinder(Arrays.asList(
        new JUnitCustomRunnerTestUnitFinder(this.config, this.excludedRunners, this.includedTestMethods),
        new ParameterisedJUnitTestFinder()));
  }

  @Override
  public TestSuiteFinder testSuiteFinder() {
    return new CompoundTestSuiteFinder(Arrays.<TestSuiteFinder> asList(
        new JUnit4SuiteFinder(), new RunnerSuiteFinder()));
  }


  @Override
  public Optional<PitHelpError> verifyEnvironment() {
    try {
      final String version = junit.runner.Version.id();
      if (isInvalidVersion(version)) {
        return Optional.ofNullable(new PitHelpError(Help.WRONG_JUNIT_VERSION, version));
      }
    } catch (final NoClassDefFoundError er) {
      return Optional.ofNullable(new PitHelpError(Help.NO_JUNIT));
    }

    return Optional.empty();
  }

  boolean isInvalidVersion(final String version) {
    try {
      final JUnitVersion jUnitVersion = JUnitVersion.parse(version);
      return jUnitVersion.isLessThan(MIN_JUNIT_VERSION);
    } catch (final IllegalArgumentException e) {
      return true;
    }
  }

}
