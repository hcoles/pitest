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
package org.pitest.mutationtest.results;

import org.pitest.functional.Option;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.execute.MutationStatusTestPair;

public class MutationResult {

  private final MutationDetails        details;
  private final MutationStatusTestPair status;

  public MutationResult(final MutationDetails md,
      final MutationStatusTestPair status) {
    this.details = md;
    this.status = status;
  }

  public MutationDetails getDetails() {
    return this.details;
  }

  public Option<String> getKillingTest() {
    return this.status.getKillingTest();
  }

  public DetectionStatus getStatus() {
    return this.status.getStatus();
  }

  public int getNumberOfTestsRun() {
    return this.status.getNumberOfTestsRun();
  }

  public String getStatusDescription() {
    for (final String test : getKillingTest()) {
      return getStatus() + " -> " + test;
    }
    return getStatus().name();
  }

}