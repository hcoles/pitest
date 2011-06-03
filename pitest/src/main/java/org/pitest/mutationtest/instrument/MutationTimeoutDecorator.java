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
package org.pitest.mutationtest.instrument;

import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestFilter;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.TestUnitDecorator;
import org.pitest.functional.Option;
import org.pitest.mutationtest.loopbreak.PerProcessTimelimitCheck;
import org.pitest.util.Monitor;
import org.pitest.util.TimeOutSystemExitSideEffect;

public final class MutationTimeoutDecorator extends TestUnitDecorator {

  private final static long           HARD_TIMEOUT_ADDIONAL_MS = 15000;
  private static final long           serialVersionUID         = 1L;

  private final TimeoutLengthStrategy timeOutStrategy;
  private final long                  executionTime;

  public MutationTimeoutDecorator(final TestUnit child,
      final TimeoutLengthStrategy timeStrategy, final long executionTime) {
    super(child);
    this.executionTime = executionTime;
    this.timeOutStrategy = timeStrategy;
  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {

    final long maxTime = this.timeOutStrategy.getEndTime(this.executionTime);
    PerProcessTimelimitCheck.setMaxEndTime(maxTime);
    final long hardTime = maxTime + HARD_TIMEOUT_ADDIONAL_MS;

    final Monitor timeoutWatchDog = new TimeoutWatchDog(
        TimeOutSystemExitSideEffect.INSTANCE, hardTime);
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
          .value(), this.timeOutStrategy, this.executionTime));
    } else {
      return Option.none();
    }

  }

}
