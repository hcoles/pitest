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
package org.pitest.junit.adapter;

import java.util.Collection;
import java.util.Stack;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.pitest.DefaultStaticConfig;
import org.pitest.Pitest;
import org.pitest.TestMethod;
import org.pitest.containers.NullContainer;
import org.pitest.containers.UnisolatedThreadPoolContainer;
import org.pitest.extension.Configuration;
import org.pitest.extension.Container;
import org.pitest.extension.TestDiscoveryListener;
import org.pitest.extension.TestFilter;
import org.pitest.extension.TestUnit;
import org.pitest.functional.F2;
import org.pitest.functional.Option;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.junit.JUnitTestResultListener;

/**
 * Custom runner to run tests with Pit but report back to junit.
 * 
 * @author henry
 * 
 */
public class PITJUnitRunner extends Runner implements Filterable {

  private final Description  description;
  private final Class<?>     root;
  private Option<TestFilter> filter = Option.none();

  public PITJUnitRunner(final Class<?> clazz) {
    this.root = clazz;
    this.description = createDescription();

  }

  private Description createDescription() {
    final Stack<Description> descriptions = new Stack<Description>();
    descriptions.push(Description.createSuiteDescription(this.root));

    final TestDiscoveryListener describer = new TestDiscoveryListener() {

      public void enterClass(final Class<?> clazz) {
        final Description childDesc = Description.createSuiteDescription(clazz);
        descriptions.add(childDesc);
      }

      public void leaveClass(final Class<?> clazz) {
        final Description thisDescription = descriptions.pop();
        // FIXME allthough this works should empty tests not be filtered out
        // before they get here??
        if (!thisDescription.getChildren().isEmpty()) {
          descriptions.peek().addChild(thisDescription);
        }
      }

      public void recieveTests(final Collection<? extends TestUnit> testUnits) {
        for (final TestUnit each : testUnits) {
          final Description d = Description.createTestDescription(each
              .description().getTestClass(), each.description().getName());
          descriptions.peek().addChild(d);
        }

      }

    };

    final Container c = new NullContainer();
    final Configuration conf = new JUnitCompatibleConfiguration();
    final DefaultStaticConfig staticConfig = createStaticConfig();
    staticConfig.addDiscoveryListener(describer);

    final Pitest pitest = new Pitest(staticConfig, conf);

    final F2<Class<?>, Container, Container> containerUpdateFunction = new F2<Class<?>, Container, Container>() {

      public Container apply(final Class<?> a, final Container b) {
        return b;
      }

    };

    pitest.run(c, containerUpdateFunction, this.root);

    return descriptions.peek().getChildren().get(0);
  }

  @Override
  public Description getDescription() {
    return this.description;
  }

  @Override
  public void run(final RunNotifier notifier) {
    System.out.println("Starting run");
    final Configuration conf = new JUnitCompatibleConfiguration();
    final DefaultStaticConfig staticConfig = createStaticConfig();
    staticConfig.getTestListeners()
        .add((new JUnitTestResultListener(notifier)));
    final Pitest pitest = new Pitest(staticConfig, conf);

    pitest.run(new UnisolatedThreadPoolContainer(1), this.root);
  }

  private DefaultStaticConfig createStaticConfig() {
    final DefaultStaticConfig staticConfig = new DefaultStaticConfig();
    for (final TestFilter each : this.filter) {
      staticConfig.getTestFilters().add(each);
    }
    return staticConfig;
  }

  public void filter(final Filter filter) throws NoTestsRemainException {
    final String description = filter.describe();
    if (description.startsWith("Method ")) {
      final String method = description.substring(7, description.indexOf("("));
      final String clazz = description.substring(description.indexOf("(") + 1,
          description.indexOf(")"));

      final TestFilter f = new TestFilter() {

        public boolean include(final TestUnit tu) {
          for (final TestMethod m : tu.description().getMethod()) {
            return (tu.description().getTestClass().getName().equals(clazz) && m
                .getName().equals(method));
          }
          return false;

        }

      };

      this.filter = Option.some(f);
    }

  }

}