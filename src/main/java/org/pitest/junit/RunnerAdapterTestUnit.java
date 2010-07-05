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
package org.pitest.junit;

import org.pitest.Description;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;
import org.pitest.testunit.AbstractTestUnit;

public class RunnerAdapterTestUnit extends AbstractTestUnit {

  private static final long                  serialVersionUID = 1L;
  private final RunnerAdapter                runner;
  private final org.junit.runner.Description junitDescription;

  public RunnerAdapterTestUnit(final RunnerAdapter runner,
      final org.junit.runner.Description junitDescription,
      final Description description, final TestUnit parent) {
    super(description, parent);
    this.runner = runner;
    this.junitDescription = junitDescription;
  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {
    this.runner.execute(loader, this, rc);
  }

  public org.junit.runner.Description getJunitDescription() {
    return this.junitDescription;
  }

}
