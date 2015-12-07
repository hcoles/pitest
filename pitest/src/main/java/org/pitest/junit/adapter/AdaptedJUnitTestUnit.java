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

import static org.pitest.util.Unchecked.translateCheckedException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.model.RunnerBuilder;
import org.pitest.functional.Option;
import org.pitest.testapi.AbstractTestUnit;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.foreignclassloader.Events;
import org.pitest.util.ClassLoaderDetectionStrategy;
import org.pitest.util.IsolationUtils;
import org.pitest.util.Log;
import org.pitest.util.Unchecked;

public class AdaptedJUnitTestUnit extends AbstractTestUnit {

  private static final Logger                LOG = Log.getLogger();

  private final ClassLoaderDetectionStrategy loaderDetection;
  private final Class<?>                     clazz;
  private final Option<Filter>               filter;

  public AdaptedJUnitTestUnit(final Class<?> clazz, final Option<Filter> filter) {
    this(IsolationUtils.loaderDetectionStrategy(), clazz, filter);
  }

  AdaptedJUnitTestUnit(final ClassLoaderDetectionStrategy loaderDetection,
      final Class<?> clazz, final Option<Filter> filter) {
    super(new org.pitest.testapi.Description(createName(clazz, filter), clazz));
    this.loaderDetection = loaderDetection;
    this.clazz = clazz;
    this.filter = filter;
  }

  private static String createName(final Class<?> clazz,
      final Option<Filter> filter) {
    if (filter.hasSome()) {
      return filter.value().describe();
    } else {
      return clazz.getName();
    }
  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {

    final Runner runner = createRunner(this.clazz);
    checkForErrorRunner(runner);
    filterIfRequired(rc, runner);

    try {
      if (this.loaderDetection.fromDifferentLoader(runner.getClass(), loader)) {
        executeInDifferentClassLoader(loader, rc, runner);

      } else {
        final CustomRunnerExecutor nativeCe = new CustomRunnerExecutor(
            this.getDescription(), runner, rc);
        nativeCe.run();
      }

    } catch (final Exception e) {
      LOG.log(Level.SEVERE, "Error while running adapter JUnit fixture "
          + this.clazz + " with filter " + this.filter, e);
      throw translateCheckedException(e);
    }

  }

  private void checkForErrorRunner(final Runner runner) {
    if (runner instanceof ErrorReportingRunner) {
      LOG.warning("JUnit error for class " + this.clazz + " : "
          + runner.getDescription());
    }

  }

  private void filterIfRequired(final ResultCollector rc, final Runner runner) {
    if (this.filter.hasSome()) {
      if (!(runner instanceof Filterable)) {
        LOG.warning("Not able to filter " + runner.getDescription()
            + ". Mutation may have prevented JUnit from constructing test");
        return;
      }
      final Filterable f = (Filterable) runner;
      try {
        f.filter(this.filter.value());
      } catch (final NoTestsRemainException e1) {
        rc.notifySkipped(this.getDescription());
        return;
      }
    }
  }

  public static Runner createRunner(final Class<?> clazz) {
    final RunnerBuilder builder = createRunnerBuilder();
    try {
      return builder.runnerForClass(clazz);
    } catch (final Throwable ex) {
      LOG.log(Level.SEVERE, "Error while creating runner for " + clazz, ex);
      throw translateCheckedException(ex);
    }

  }

  private static RunnerBuilder createRunnerBuilder() {
    return new AllDefaultPossibilitiesBuilder(true);
  }

  private void executeInDifferentClassLoader(final ClassLoader loader,
      final ResultCollector rc, final Runner runner)
          throws IllegalAccessException, InvocationTargetException {

    // must jump through hoops to run in different class loader
    // when even our framework classes may be duplicated
    // translate everything via strings
    final ForeignClassLoaderCustomRunnerExecutor ce = new ForeignClassLoaderCustomRunnerExecutor(
        runner);
    @SuppressWarnings("unchecked")
    Callable<List<String>> foreignCe = (Callable<List<String>>) IsolationUtils
    .cloneForLoader(ce, loader);

    try {
      final List<String> q = foreignCe.call();
      convertStringsToResults(rc, q);
    } catch (Exception ex) {
      throw Unchecked.translateCheckedException(ex);
    }

  }

  private void convertStringsToResults(final ResultCollector rc,
      final List<String> q) {
    Events.applyEvents(q, rc, this.getDescription());
  }

  @Override
  public String toString() {
    return "AdaptedJUnitTestUnit [clazz=" + this.clazz + ", filter="
        + this.filter + "]";
  }

}
