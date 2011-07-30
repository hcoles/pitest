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
package org.pitest.mutationtest.execute;

import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestFilter;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.TestUnitDecorator;
import org.pitest.functional.Option;
import org.pitest.mutationtest.instrument.TimeoutLengthStrategy;
import org.pitest.util.Monitor;
import org.pitest.util.TimeOutSystemExitSideEffect;

public final class MutationTimeoutDecorator extends TestUnitDecorator {

  private static final long           serialVersionUID = 1L;

  private final TimeoutLengthStrategy timeOutStrategy;
  private final long                  executionTime;
  private final Reporter              r;

  public MutationTimeoutDecorator(final TestUnit child,
      final TimeoutLengthStrategy timeStrategy, final long executionTime,
      final Reporter r) {
    super(child);
    this.executionTime = executionTime;
    this.timeOutStrategy = timeStrategy;
    this.r = r;
  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {

    final long maxTime = this.timeOutStrategy.getEndTime(this.executionTime);

    final Monitor timeoutWatchDog = new TimeoutWatchDog(
        new TimeOutSystemExitSideEffect(this.r), maxTime);
    timeoutWatchDog.requestStart();
    try {
      this.child().execute(loader, rc);
    } finally {
      timeoutWatchDog.requestStop();
    }

  }

  public Option<TestUnit> filter(final TestFilter filter) {
    final Option<TestUnit> modifiedChild = this.child().filter(filter);
    if (modifiedChild.hasSome()) {
      return Option.<TestUnit> some(new MutationTimeoutDecorator(modifiedChild
          .value(), this.timeOutStrategy, this.executionTime, this.r));
    } else {
      return Option.none();
    }

  }

}
