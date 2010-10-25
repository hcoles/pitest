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

import static org.pitest.util.Unchecked.translateCheckedException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;
import org.pitest.TestMethod;
import org.pitest.extension.ResultCollector;
import org.pitest.internal.IsolationUtils;
import org.pitest.junit.CustomRunnerExecutor;
import org.pitest.junit.PossibilitiesBuilder;
import org.pitest.reflection.Reflection;
import org.pitest.testunit.AbstractTestUnit;

public class RunnerAdapter extends AbstractTestUnit {

  private static final long                            serialVersionUID = 1L;

  private final List<RunnerAdapterDescriptionTestUnit> descriptions;
  private final Class<?>                               clazz;

  public RunnerAdapter(final Class<?> clazz, final Runner runner) {
    super(new org.pitest.Description("AdapterForCustomJunitTest", clazz, null));
    this.clazz = clazz;
    this.descriptions = new ArrayList<RunnerAdapterDescriptionTestUnit>();
    gatherDescriptions(this.descriptions, createRunner(clazz).getDescription());
  }

  public RunnerAdapter(final Class<?> clazz) {
    this(clazz, createRunner(clazz));
  }

  private static Runner createRunner(final Class<?> clazz) {
    final RunnerBuilder builder = createRunnerBuilder(clazz);
    try {
      return builder.runnerForClass(clazz);
    } catch (final Throwable ex) {
      ex.printStackTrace();
      throw translateCheckedException(ex);
    }

  }

  private static RunnerBuilder createRunnerBuilder(final Class<?> clazz) {
    return new PossibilitiesBuilder(true);
  }

  private void gatherDescriptions(
      final List<RunnerAdapterDescriptionTestUnit> tus, final Description d) {
    if (d.isTest() && !d.getClassName().startsWith("junit.framework")) {
      tus.add(descriptionToTestUnit(d));
    } else {
      for (final Description each : d.getChildren()) {
        gatherDescriptions(tus, each);
      }
    }
  }

  private RunnerAdapterDescriptionTestUnit descriptionToTestUnit(
      final Description d) {

    Class<?> descriptionTestClass;
    try {
      // it would be nice is junit could use the context class loader . . .
      descriptionTestClass = Class.forName(d.getClassName(), false,
          IsolationUtils.getContextClassLoader());
    } catch (final ClassNotFoundException e) {
      descriptionTestClass = null;
    }

    final Method m = Reflection.publicMethod(descriptionTestClass, d
        .getMethodName());
    final TestMethod tm = new TestMethod(m, null);

    final org.pitest.Description pitDescription = new org.pitest.Description(d
        .getDisplayName(), descriptionTestClass, tm);

    return new RunnerAdapterDescriptionTestUnit(d, pitDescription);
  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {
    final Runner runner = createRunner(this.clazz);
    final CustomRunnerExecutor ce = new CustomRunnerExecutor(runner, rc,
        this.descriptions);
    Object foreignCe = ce;
    if (IsolationUtils.fromDifferentLoader(runner.getClass(), loader)) {
      foreignCe = IsolationUtils.cloneForLoader(ce, loader);
    }
    final Method run = Reflection.publicMethod(foreignCe.getClass(), "run");
    try {
      run.invoke(foreignCe);
    } catch (final Exception e) {
      throw translateCheckedException(e);
    }

  }

  public Class<?> getClazz() {
    return this.clazz;
  }

  public List<RunnerAdapterDescriptionTestUnit> getDescriptions() {
    return this.descriptions;
  }

}
