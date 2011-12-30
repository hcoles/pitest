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

import org.pitest.classinfo.Repository;
import org.pitest.extension.Configuration;
import org.pitest.extension.common.CompoundConfiguration;
import org.pitest.internal.ClassByteArraySource;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.testng.TestGroupConfig;
import org.pitest.testng.TestNGConfiguration;

public class ConfigurationFactory {

  private final ClassByteArraySource source;
  private final TestGroupConfig      config;

  public ConfigurationFactory(final TestGroupConfig config,
      final ClassByteArraySource source) {
    this.source = source;
    this.config = config;
  }

  public Configuration createConfiguration() {
    final Collection<Configuration> configs = new ArrayList<Configuration>();
    final Repository classRepository = new Repository(this.source);

    if (classRepository.fetchClass("org.junit.runner.Runner").hasSome()) {
      configs.add(new JUnitCompatibleConfiguration());
    }

    if (classRepository.fetchClass("org.testng.TestNG").hasSome()) {
      configs.add(new TestNGConfiguration(this.config));
    }

    return new CompoundConfiguration(configs);
  }

}
