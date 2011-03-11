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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.pitest.Description;
import org.pitest.TestMethod;
import org.pitest.extension.ResultCollector;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.IsolationUtils;
import org.pitest.reflection.IsNamed;
import org.pitest.reflection.Reflection;
import org.pitest.testunit.AbstractTestUnit;
import org.pitest.util.Unchecked;

public class TestAdapter extends AbstractTestUnit {

  private final String                    testMethod;
  private final Class<? extends TestCase> testClass;

  private static class SetJUnit3Name implements Predicate<Method> {
    private final static SetJUnit3Name INSTANCE = new SetJUnit3Name();

    public static SetJUnit3Name instance() {
      return INSTANCE;
    }

    public Boolean apply(final Method a) {
      return a.getName().equals("setName")
          && (a.getParameterTypes().length == 1)
          && a.getParameterTypes()[0].equals(String.class);
    }

  }

  private static class RunBareMethod implements Predicate<Method> {
    private final static RunBareMethod INSTANCE = new RunBareMethod();

    public static RunBareMethod instance() {
      return INSTANCE;
    }

    public Boolean apply(final Method a) {
      return a.getName().equals("runBare")
          && (a.getParameterTypes().length == 0);
    }

  }

  public TestAdapter(final TestCase testCase) {
    super(testCaseToDescription(testCase));
    this.testMethod = testCase.getName();
    this.testClass = testCase.getClass();
  }

  private static Description testCaseToDescription(final TestCase testCase) {
    final Collection<Method> ms = Reflection.allMethods(testCase.getClass());
    final List<Method> filteredMethods = FCollection.filter(ms,
        IsNamed.instance(testCase.getName()));
    if (filteredMethods.isEmpty()) {
      return new Description(testCase.getName(), testCase.getClass(), null);
    } else {
      return new Description(testCase.getName(), testCase.getClass(),
          new TestMethod(filteredMethods.get(0)));
    }

  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {
    try {
      final Class<?> activeClass = IsolationUtils.convertForClassLoader(loader,
          this.testClass);

      rc.notifyStart(getDescription());
      final Object test = createTest(activeClass, this.testMethod);
      final Method runBare = Reflection.publicMethod(activeClass,
          RunBareMethod.instance());
      try {
        runBare.invoke(test);
        rc.notifyEnd(getDescription());
      } catch (final Throwable t) {
        rc.notifyEnd(getDescription(), t.getCause());
      }

    } catch (final Throwable t) {
      rc.notifyEnd(getDescription(), t);
    }
  }

  static public Object createTest(final Class<?> theClass, final String name) {

    Object test;
    try {
      final Constructor<?> constructor = getTestConstructor(theClass);

      if (constructor.getParameterTypes().length == 0) {
        test = constructor.newInstance(new Object[0]);
        final Method setName = Reflection.publicMethod(theClass,
            SetJUnit3Name.instance());
        setName.invoke(test, new Object[] { name });
      } else {
        test = constructor.newInstance(new Object[] { name });
      }

    } catch (final Exception e) {
      throw Unchecked.translateCheckedException(e);
    }

    return test;
  }

  /**
   * Gets a constructor which takes a single String as its argument or a no arg
   * constructor.
   */
  public static Constructor<?> getTestConstructor(final Class<?> theClass)
      throws NoSuchMethodException {
    try {
      return theClass.getConstructor(String.class);
    } catch (final NoSuchMethodException e) {
      // fall through
    }
    return theClass.getConstructor(new Class[0]);
  }

}
