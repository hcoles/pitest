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

import java.util.ArrayList;
import java.util.Collection;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.Repository;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testng.TestNGConfiguration;

class ConfigurationFactory {

  private final ClassByteArraySource source;
  private final TestGroupConfig      config;
  private final Collection<String> excludedRunners;

  ConfigurationFactory(final TestGroupConfig config,
      final ClassByteArraySource source, Collection<String> excludedRunners) {
    this.source = source;
    this.config = config;
    this.excludedRunners = excludedRunners;
  }

  Configuration createConfiguration() {
    final Collection<Configuration> configs = new ArrayList<Configuration>();
    final Repository classRepository = new Repository(this.source);

    if (classRepository.fetchClass(ClassName.fromString("org.junit.Test"))
        .hasSome()) {
      configs.add(new JUnitCompatibleConfiguration(this.config, this.excludedRunners));
    }

    if (classRepository.fetchClass(ClassName.fromString("org.testng.TestNG"))
        .hasSome()) {
      configs.add(new TestNGConfiguration(this.config));
    }

    if (configs.isEmpty()) {
      throw new PitHelpError(Help.NO_TEST_LIBRARY);
    }

    return new CompoundConfiguration(configs);
  }

}
