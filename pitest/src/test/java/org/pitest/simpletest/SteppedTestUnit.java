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
package org.pitest.simpletest;

import java.util.Collection;

import org.pitest.functional.Option;
import org.pitest.testapi.AbstractTestUnit;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;
import org.pitest.util.IsolationUtils;

/**
 * @author henry
 *
 */
public class SteppedTestUnit extends AbstractTestUnit {

  private final Collection<TestStep>               steps;
  private final Option<Class<? extends Throwable>> expected;

  public SteppedTestUnit(final Description description,
      final Collection<TestStep> steps,
      final Option<Class<? extends Throwable>> expected) {
    super(description);
    this.steps = steps;
    this.expected = expected;
  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {
    if (!this.steps().isEmpty()) {
      executeStepsAndReport(loader, rc);
    } else {
      rc.notifySkipped(this.getDescription());
    }

  }

  private void executeStepsAndReport(final ClassLoader loader,
      final ResultCollector rc) {
    rc.notifyStart(this.getDescription());
    Object o = null;
    Throwable tResult = null;
    try {
      for (final TestStep s : this.steps) {
        o = s.execute(loader, getDescription(), o);
      }
    } catch (final TestExecutionException tee) {
      tResult = tee.getCause();
    } catch (final Throwable t) {
      tResult = t;
    }

    tResult = updateResultForExpectations(loader, tResult);

    rc.notifyEnd(this.getDescription(), tResult);
  }

  private Throwable updateResultForExpectations(final ClassLoader loader,
      final Throwable tResult) {
    if (this.expected.hasSome()) {
      if (tResult == null) {
        return new java.lang.AssertionError("Expected exception "
            + this.expected);
      } else if (IsolationUtils.convertForClassLoader(loader,
          this.expected.value()).isAssignableFrom(tResult.getClass())) {
        return null;
      }
    }

    return tResult;
  }

  private Collection<TestStep> steps() {
    return this.steps;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = (prime * result)
        + ((this.expected == null) ? 0 : this.expected.hashCode());
    result = (prime * result)
        + ((this.steps == null) ? 0 : this.steps.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SteppedTestUnit other = (SteppedTestUnit) obj;
    if (this.expected == null) {
      if (other.expected != null) {
        return false;
      }
    } else if (!this.expected.equals(other.expected)) {
      return false;
    }
    if (this.steps == null) {
      if (other.steps != null) {
        return false;
      }
    } else if (!this.steps.equals(other.steps)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "SteppedTestUnit [steps=" + this.steps + ", expected="
        + this.expected + ", getDescription()=" + getDescription() + "]";
  }

}
