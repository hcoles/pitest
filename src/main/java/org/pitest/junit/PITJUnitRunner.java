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

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.pitest.ConcreteConfiguration;
import org.pitest.Pitest;
import org.pitest.StaticConfig;
import org.pitest.containers.UnisolatedThreadPoolContainer;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestUnit;
import org.pitest.internal.TestClass;

/**
 * Custom runner to run tests with Pit but report back to junit.
 * 
 * @author henry
 * 
 */
public class PITJUnitRunner extends Runner {

  private final Description   description;
  private final Pitest        pitest;
  private final Class<?>      root;
  private final Configuration config = new JUnitCompatibleConfiguration();

  public PITJUnitRunner(final Class<?> clazz) {
    this.root = clazz;
    this.pitest = new Pitest(this.config);

    this.description = Description.createSuiteDescription(clazz);

    findAndDescribeTestUnits(new TestClass(clazz), this.description,
        this.config);
  }

  private void findAndDescribeTestUnits(final TestClass root,
      final Description description, final Configuration startConfig) {

    for (final TestUnit tu : root.getTestUnitsWithinClass(startConfig)) {
      final Description d = Description.createTestDescription(tu.description()
          .getTestClass(), tu.description().getName());
      description.addChild(d);

    }

    final Configuration updatedConfig = ConcreteConfiguration.updateConfig(
        startConfig, root);

    for (final TestClass tc : root.getChildren(updatedConfig)) {
      final Description childDesc = Description.createSuiteDescription(tc
          .getClazz());
      description.addChild(childDesc);

      findAndDescribeTestUnits(tc, childDesc, updatedConfig);

    }

  }

  @Override
  public Description getDescription() {
    return this.description;
  }

  @Override
  public void run(final RunNotifier notifier) {
    final StaticConfig staticConfig = new StaticConfig();
    staticConfig.getTestListeners()
        .add((new JUnitTestResultListener(notifier)));
    this.pitest.run(new UnisolatedThreadPoolContainer(1), staticConfig,
        this.root);
  }

}
