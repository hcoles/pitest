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

import org.pitest.Description;
import org.pitest.extension.ResultCollector;
import org.pitest.testunit.AbstractTestUnit;

public class RunnerAdapterDescriptionTestUnit extends AbstractTestUnit {

  private final org.junit.runner.Description junitDescription;

  public RunnerAdapterDescriptionTestUnit(final org.junit.runner.Description d,
      final Description description) {
    super(description);
    this.junitDescription = d;
  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {
    // Should never be called.
    assert (false);

  }

  public org.junit.runner.Description getJunitDescription() {
    return this.junitDescription;
  }

}
