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

import org.pitest.functional.F;
import org.pitest.functional.SideEffect1;
import org.pitest.testapi.TestListener;
import org.pitest.testapi.TestResult;

/**
 * @author henry
 *
 */
public enum ResultType {

  PASS(new ResultToListenerSideEffect() {
    @Override
    public SideEffect1<TestListener> apply(final TestResult a) {
      return success(a);
    }
  }),

  FAIL(new ResultToListenerSideEffect() {
    @Override
    public SideEffect1<TestListener> apply(final TestResult a) {
      return failure(a);
    }
  }),

  SKIPPED(new ResultToListenerSideEffect() {
    @Override
    public SideEffect1<TestListener> apply(final TestResult a) {
      return skipped(a);
    }
  }),

  STARTED(new ResultToListenerSideEffect() {
    @Override
    public SideEffect1<TestListener> apply(final TestResult a) {
      return started(a);
    }
  });

  private interface ResultToListenerSideEffect extends
  F<TestResult, SideEffect1<TestListener>> {
  };

  ResultType(final ResultToListenerSideEffect f) {
    this.function = f;
  }

  private final F<TestResult, SideEffect1<TestListener>> function;

  public SideEffect1<TestListener> getListenerFunction(final TestResult result) {
    return this.function.apply(result);
  };

  public static SideEffect1<TestListener> success(final TestResult result) {
    return new SideEffect1<TestListener>() {
      @Override
      public void apply(final TestListener a) {
        a.onTestSuccess(result);
      }
    };
  }

  public static SideEffect1<TestListener> failure(final TestResult result) {
    return new SideEffect1<TestListener>() {
      @Override
      public void apply(final TestListener a) {
        a.onTestFailure(result);
      }
    };
  }

  public static SideEffect1<TestListener> skipped(final TestResult result) {
    return new SideEffect1<TestListener>() {
      @Override
      public void apply(final TestListener a) {
        a.onTestSkipped(result);
      }
    };
  }

  public static SideEffect1<TestListener> started(final TestResult result) {
    return new SideEffect1<TestListener>() {
      @Override
      public void apply(final TestListener a) {
        a.onTestStart(result.getDescription());
      }
    };
  }

}
