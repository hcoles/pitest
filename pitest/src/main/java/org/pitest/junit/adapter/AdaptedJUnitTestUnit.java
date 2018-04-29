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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.model.RunnerBuilder;
import java.util.Optional;
import org.pitest.testapi.AbstractTestUnit;
import org.pitest.testapi.ResultCollector;
import org.pitest.util.Log;

public class AdaptedJUnitTestUnit extends AbstractTestUnit {

  private static final Logger                LOG = Log.getLogger();

  private final Class<?>                     clazz;
  private final Optional<Filter>               filter;


  public AdaptedJUnitTestUnit(
      final Class<?> clazz, final Optional<Filter> filter) {
    super(new org.pitest.testapi.Description(createName(clazz, filter), clazz));
    this.clazz = clazz;
    this.filter = filter;
  }

  private static String createName(final Class<?> clazz,
      final Optional<Filter> filter) {
    if (filter.isPresent()) {
      return filter.get().describe();
    } else {
      return clazz.getName();
    }
  }

  @Override
  public void execute(final ResultCollector rc) {

    final Runner runner = createRunner(this.clazz);
    checkForErrorRunner(runner);
    filterIfRequired(rc, runner);

    try {
        final CustomRunnerExecutor nativeCe = new CustomRunnerExecutor(
            this.getDescription(), runner, rc);
        nativeCe.run();

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
    if (this.filter.isPresent()) {
      if (!(runner instanceof Filterable)) {
        LOG.warning("Not able to filter " + runner.getDescription()
            + ". Mutation may have prevented JUnit from constructing test");
        return;
      }
      final Filterable f = (Filterable) runner;
      try {
        f.filter(this.filter.get());
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

  @Override
  public String toString() {
    return "AdaptedJUnitTestUnit [clazz=" + this.clazz + ", filter="
        + this.filter + "]";
  }

}
