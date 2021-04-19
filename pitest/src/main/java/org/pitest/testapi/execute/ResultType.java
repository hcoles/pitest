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
package org.pitest.testapi.execute;

import org.pitest.testapi.TestListener;
import org.pitest.testapi.TestResult;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author henry
 *
 */
public enum ResultType {

  PASS(ResultType::success),

  FAIL(ResultType::failure),

  SKIPPED(ResultType::skipped),

  STARTED(ResultType::started);

  private interface ResultToListenerSideEffect extends
  Function<TestResult, Consumer<TestListener>> {
  }

  ResultType(final ResultToListenerSideEffect f) {
    this.function = f;
  }

  private final Function<TestResult, Consumer<TestListener>> function;

  public Consumer<TestListener> getListenerFunction(final TestResult result) {
    return this.function.apply(result);
  }

  public static Consumer<TestListener> success(final TestResult result) {
    return a -> a.onTestSuccess(result);
  }

  public static Consumer<TestListener> failure(final TestResult result) {
    return a -> a.onTestFailure(result);
  }

  public static Consumer<TestListener> skipped(final TestResult result) {
    return a -> a.onTestSkipped(result);
  }

  public static Consumer<TestListener> started(final TestResult result) {
    return a -> a.onTestStart(result.getDescription());
  }

}
