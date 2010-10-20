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
package org.pitest.mutationtest;

import org.pitest.ConcreteConfiguration;
import org.pitest.extension.Configuration;
import org.pitest.extension.ConfigurationUpdater;

public class MutationSuiteConfigUpdater implements ConfigurationUpdater {

  private final static MutationSuiteConfigUpdater instance = new MutationSuiteConfigUpdater();

  private MutationSuiteConfigUpdater() {

  }

  public static MutationSuiteConfigUpdater instance() {
    return instance;
  }

  public Configuration updateConfiguration(final Class<?> clazz,
      final Configuration current) {
    final MutationTest annotation = clazz.getAnnotation(MutationTest.class);
    if (annotation != null) {
      return update(annotation, current);
    } else {
      return current;
    }
  }

  private Configuration update(final MutationTest annotation,
      final Configuration current) {
    final MutationConfig config = new MutationConfig(annotation.useHotSwap(),
        annotation.threshold(), annotation.mutators());
    final MutationTestFinder msf = new MutationTestFinder(config);
    final Configuration copy = new ConcreteConfiguration(current);
    copy.testUnitFinders().add(msf);
    return copy;
  }

}
