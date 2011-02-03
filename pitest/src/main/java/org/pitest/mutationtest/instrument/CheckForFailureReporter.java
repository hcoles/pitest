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

import java.io.IOException;

import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus;

public class CheckForFailureReporter implements Reporter {

  private boolean hadFailure;

  public boolean hadFailure() {
    return this.hadFailure;
  }

  public void describe(final MutationIdentifier i, final int numberOfTests,
      final Mutant mutatedClass) throws IOException {
    // noop

  }

  public void report(final MutationIdentifier i,
      final DetectionStatus mutationDetected) throws IOException {
    this.hadFailure = mutationDetected.isDetected();

  }

}
