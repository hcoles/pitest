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
package org.pitest.testng;

import org.pitest.extension.ResultCollector;
import org.pitest.testunit.AbstractTestUnit;
import org.testng.ITestListener;
import org.testng.TestNG;

public class TestNGTestUnit extends AbstractTestUnit {

  private final Class<?> clazz;

  public TestNGTestUnit(final Class<?> clazz) {
    super(new org.pitest.Description(clazz.getName(), clazz));
    this.clazz = clazz;
  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {

    // take a look at testnmethodfinder

    final ITestListener listener = new TestNGAdapter(this.getDescription(), rc);
    final TestNG testng = new TestNG();
    testng.setUseDefaultListeners(false);
    testng.setTestClasses(new Class[] { this.clazz });
    testng.addListener(listener);
    testng.run();
  }

}
