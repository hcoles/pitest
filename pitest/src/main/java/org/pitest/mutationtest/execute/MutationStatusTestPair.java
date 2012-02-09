/*
 * Copyright 2011 Henry Coles
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

import org.pitest.functional.Option;
import org.pitest.mutationtest.results.DetectionStatus;

public class MutationStatusTestPair {

  private final int             numberOfTestsRun;
  private final DetectionStatus status;
  private final Option<String>  killingTest;

  public MutationStatusTestPair(final int numberOfTestsRun,
      final DetectionStatus status) {
    this(numberOfTestsRun, status, null);
  }

  public MutationStatusTestPair(final int numberOfTestsRun,
      final DetectionStatus status, final String killingTest) {
    this.status = status;
    this.killingTest = Option.some(killingTest);
    this.numberOfTestsRun = numberOfTestsRun;
  }

  public DetectionStatus getStatus() {
    return this.status;
  }

  public Option<String> getKillingTest() {
    return this.killingTest;
  }

  public int getNumberOfTestsRun() {
    return this.numberOfTestsRun;
  }

  @Override
  public String toString() {
    if (this.killingTest.hasNone()) {
      return this.status.name();
    } else {
      return this.status.name() + " by " + this.killingTest.value();
    }

  }

}
