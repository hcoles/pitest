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
package org.pitest.testapi;

import java.util.Objects;

/**
 * @author henry
 *
 */
public final class TestResult {

  private final Description   description;

  private final Throwable     throwable;
  private final TestUnitState state;

  public TestResult(final Description description, final Throwable t) {
    this(description, t, TestUnitState.FINISHED);
  }

  public TestResult(final Description description, final Throwable t,
      final TestUnitState state) {
    this.description = description;
    this.throwable = t;
    this.state = state;
  }

  public Throwable getThrowable() {
    return this.throwable;
  }

  public TestUnitState getState() {
    return this.state;
  }

  public Description getDescription() {
    return this.description;
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, throwable, state);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final TestResult other = (TestResult) obj;
    return Objects.equals(description, other.description)
            && Objects.equals(throwable, other.throwable)
            && state == other.state;
  }

  @Override
  public String toString() {
    return "TestResult [description=" + this.description + ", state="
        + this.state + ", throwable=" + this.throwable + "]";
  }

}
