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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.internal.builders.AnnotatedBuilder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.pitest.TestMethod;
import org.pitest.TestResult;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;
import org.pitest.internal.IsolationUtils;
import org.pitest.reflection.Reflection;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.Sun14ReflectionProvider;

public class RunnerAdapter implements Serializable {

  private static final long                serialVersionUID = 1L;

  private transient Runner                 runner;
  private transient Map<String, Throwable> results;
  private transient List<TestUnit>         tus              = new ArrayList<TestUnit>();

  private final Class<?>                   clazz;

  public RunnerAdapter(final Class<?> clazz) {
    this.clazz = clazz;
    this.runner = createRunner(clazz);
    gatherTestUnits(this.tus, this.runner.getDescription());
  }

  private Runner createRunner(final Class<?> clazz) {
    final AnnotatedBuilder builder = new AnnotatedBuilder(
        new AllDefaultPossibilitiesBuilder(true));
    try {
      return builder.runnerForClass(clazz);
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public List<TestUnit> getTestUnits() {
    return this.tus;
  }

  private void gatherTestUnits(final List<TestUnit> tus, final Description d) {
    if (d.isTest()) {
      tus.add(descriptionToTestUnit(d));
    } else {
      for (final Description each : d.getChildren()) {
        gatherTestUnits(tus, each);
      }
    }
  }

  private TestUnit descriptionToTestUnit(final Description d) {

    Class<?> descriptionTestClass;
    try {
      // it would be nice is junit could use the context class loader . . .
      descriptionTestClass = Class.forName(d.getClassName(), true, Thread
          .currentThread().getContextClassLoader());
    } catch (final ClassNotFoundException e) {
      descriptionTestClass = null;
    }
    System.out.println("m is " + descriptionTestClass);

    final Method m = Reflection.publicMethod(descriptionTestClass, d
        .getMethodName());
    final TestMethod tm = new TestMethod(m, null);
    final org.pitest.Description pitDescription = new org.pitest.Description(d
        .getDisplayName(), d.getTestClass(), tm);
    return new RunnerAdapterTestUnit(this, d, pitDescription, null);
  }

  public void execute(final ClassLoader loader,
      final RunnerAdapterTestUnit testUnit, final ResultCollector rc) {
    rc.notifyStart(testUnit);
    runIfRequired(loader);
    notify(testUnit, rc);
  }

  private void notify(final RunnerAdapterTestUnit testUnit,
      final ResultCollector rc) {
    final Throwable t = this.results.get(CustomRunnerExecutor
        .descriptionToString(testUnit.getJunitDescription()));
    if (t != null) {
      // TODO translate the throwable + isn't there a potential classloader leak
      // here?
      final TestResult testResult = new TestResult(testUnit, t);
      rc.notifyEnd(testResult);
    } else {
      final TestResult testResult = new TestResult(testUnit, null);
      rc.notifyEnd(testResult);
    }

  }

  @SuppressWarnings("unchecked")
  private void runIfRequired(final ClassLoader loader) {

    if (this.runner != null) {
      final CustomRunnerExecutor ce = new CustomRunnerExecutor(this.runner);
      Object foreignCe = ce;
      if (IsolationUtils.fromDifferentLoader(this.runner.getClass(), loader)) {
        foreignCe = cloneExecutorForLoader(ce, loader);
      }
      final Method run = Reflection.publicMethod(foreignCe.getClass(), "run");
      try {
        this.results = (Map<String, Throwable>) run.invoke(foreignCe);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }

      this.runner = null;
    }

  }

  private Object cloneExecutorForLoader(final CustomRunnerExecutor ce,
      final ClassLoader loader) {
    try {
      final XStream xstream = new XStream();
      final String xml = xstream.toXML(ce);
      final XStream foreginXstream = new XStream(new Sun14ReflectionProvider());
      foreginXstream.setClassLoader(loader);

      return foreginXstream.fromXML(xml);
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }

  }

  private void readObject(final ObjectInputStream aInputStream)
      throws ClassNotFoundException, IOException {

    aInputStream.defaultReadObject();
    this.runner = createRunner(this.clazz);
    this.tus = new ArrayList<TestUnit>();
    gatherTestUnits(this.tus, this.runner.getDescription());

  }

  public Class<?> getClazz() {
    return this.clazz;
  }

  public Description getTestUnitDescriptionForString(final String description) {
    for (final TestUnit each : this.tus) {
      final RunnerAdapterTestUnit rutu = (RunnerAdapterTestUnit) each;
      if (CustomRunnerExecutor.descriptionToString(rutu.getJunitDescription())
          .equals(description)) {
        return rutu.getJunitDescription();
      }
    }
    return null;
  }

}
