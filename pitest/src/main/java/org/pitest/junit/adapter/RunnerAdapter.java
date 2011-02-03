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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;
import org.pitest.TestMethod;
import org.pitest.extension.ResultCollector;
import org.pitest.internal.IsolationUtils;
import org.pitest.junit.CustomRunnerExecutor;
import org.pitest.junit.ForeignClassLoaderCustomRunnerExecutor;
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

  public static Runner createRunner(final Class<?> clazz) {
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

    final Method m = Reflection.publicMethod(descriptionTestClass,
        d.getMethodName());
    final TestMethod tm = new TestMethod(m);

    final org.pitest.Description pitDescription = new org.pitest.Description(
        d.getDisplayName(), descriptionTestClass, tm);

    return new RunnerAdapterDescriptionTestUnit(d, pitDescription);
  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {
    final Runner runner = createRunner(this.clazz);

    try {

      final Map<String, org.pitest.Description> descriptionLookup = createDescriptionLookupMap(this.descriptions);

      if (IsolationUtils.fromDifferentLoader(runner.getClass(), loader)) {
        executeInDifferentClassLoader(loader, rc, runner, descriptionLookup);

      } else {
        final CustomRunnerExecutor nativeCe = new CustomRunnerExecutor(runner,
            rc, descriptionLookup);
        nativeCe.run();
      }

    } catch (final Exception e) {
      throw translateCheckedException(e);
    }

  }

  private void executeInDifferentClassLoader(final ClassLoader loader,
      final ResultCollector rc, final Runner runner,
      final Map<String, org.pitest.Description> descriptionLookup)
      throws IllegalAccessException, InvocationTargetException {

    // must jump through hoops to run in different class loader
    // when even our framework classes may be duplicated
    // tanslate everything via stirngs
    final List<String> q = new ArrayList<String>(runner.testCount() * 2);
    final ForeignClassLoaderCustomRunnerExecutor ce = new ForeignClassLoaderCustomRunnerExecutor(
        runner);
    Object foreignCe = ce;
    foreignCe = IsolationUtils.cloneForLoader(ce, loader);
    final Method run = Reflection.publicMethod(foreignCe.getClass(), "run");

    // set an uncloned list to receive results in
    final Method set = Reflection
        .publicMethod(foreignCe.getClass(), "setQueue");
    set.invoke(foreignCe, q);
    run.invoke(foreignCe);

    for (final String each : q) {
      final String results[] = each.split(",");
      final String type = results[0];
      final String test = results[1];
      final org.pitest.Description d = descriptionLookup.get(test);
      if (type.equals("FAIL")) {
        final Throwable t = (Throwable) IsolationUtils
            .fromTransportString(results[2]);
        rc.notifyEnd(d, t);
      } else if (type.equals("IGNORE")) {
        rc.notifySkipped(d);

      } else if (type.equals("START")) {
        rc.notifyStart(d);

      } else if (type.equals("END")) {
        rc.notifyEnd(d);
      }
    }
  }

  private Map<String, org.pitest.Description> createDescriptionLookupMap(
      final List<RunnerAdapterDescriptionTestUnit> descriptions) {
    final HashMap<String, org.pitest.Description> descriptionLookup = new HashMap<String, org.pitest.Description>();
    for (final RunnerAdapterDescriptionTestUnit each : descriptions) {
      // map against string representation of description to avoid
      // equality issues with multiple classloaders
      descriptionLookup.put(
          CustomRunnerExecutor.descriptionToString(each.getJunitDescription()),
          each.getDescription());
    }
    return descriptionLookup;
  }

  public Class<?> getClazz() {
    return this.clazz;
  }

  public List<RunnerAdapterDescriptionTestUnit> getDescriptions() {
    return this.descriptions;
  }

}
