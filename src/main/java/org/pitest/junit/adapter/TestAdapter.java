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

import java.lang.reflect.Method;
import java.util.Collection;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.pitest.Description;
import org.pitest.TestMethod;
import org.pitest.extension.ResultCollector;
import org.pitest.functional.FCollection;
import org.pitest.internal.IsolationUtils;
import org.pitest.reflection.IsNamed;
import org.pitest.reflection.Reflection;
import org.pitest.testunit.AbstractTestUnit;

public class TestAdapter extends AbstractTestUnit {

  private final String                    testMethod;
  private final Class<? extends TestCase> testClass;

  public TestAdapter(final TestCase testCase) {
    super(testCaseToDescription(testCase));
    this.testMethod = testCase.getName();
    this.testClass = testCase.getClass();
  }

  private static Description testCaseToDescription(final TestCase testCase) {
    final Collection<Method> ms = Reflection.allMethods(testCase.getClass());
    final Method m = FCollection.filter(ms,
        IsNamed.instance(testCase.getName())).get(0);
    return new Description(testCase.getName(), testCase.getClass(),
        new TestMethod(m, null));
  }

  @SuppressWarnings("unchecked")
  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {
    try {
      final Class<? extends TestCase> activeClass = (Class<? extends TestCase>) IsolationUtils
          .convertForClassLoader(loader, this.testClass);
      final TestCase tc = (TestCase) TestSuite.createTest(activeClass,
          this.testMethod);
      rc.notifyStart(description());
      tc.runBare();
      rc.notifyEnd(description());

    } catch (final Throwable t) {
      rc.notifyEnd(description(), t);
    }
  }

}
