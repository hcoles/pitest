package org.pitest.junit;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;
import org.pitest.extension.Configuration;
import org.pitest.extension.InstantiationStrategy;
import org.pitest.extension.MethodFinder;
import org.pitest.extension.TestDiscoveryListener;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitFinder;
import org.pitest.extension.TestUnitProcessor;
import org.pitest.extension.common.BasicTestUnitFinder;
import org.pitest.extension.common.NamedTestSingleStringConstructorInstantiationStrategy;
import org.pitest.extension.common.NoArgsConstructorInstantiationStrategy;
import org.pitest.extension.common.SimpleAnnotationTestMethodFinder;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.TestClass;
import org.pitest.junit.adapter.AbstractPITJUnitRunner;
import org.pitest.reflection.Reflection;

public class NativeJUnitTestFinder implements TestUnitFinder {

  private final BasicTestUnitFinder impl;

  public static boolean canHandleNatively(final Class<?> clazz) {
    return !unhandledRunWith(clazz) && !hasMethodRule(clazz)
        && !CustomJUnit3TestUnitFinder.isCustomJUnit3Class(clazz);
  }

  private static boolean unhandledRunWith(final Class<?> clazz) {
    final RunWith runWith = clazz.getAnnotation(RunWith.class);
    if (runWith != null) {
      return runwithNotHandledNatively(runWith);
    } else {
      return false;
    }
  }

  public static boolean runwithNotHandledNatively(final RunWith runWith) {
    return (runWith != null)
        && !AbstractPITJUnitRunner.class.isAssignableFrom(runWith.value())
        && !runWith.value().equals(Suite.class)
        && !runWith.value().equals(Parameterized.class);
  }

  public static boolean hasMethodRule(final Class<?> clazz) {
    final Predicate<Field> p = new Predicate<Field>() {
      public Boolean apply(final Field a) {
        try {
          return a.isAnnotationPresent(Rule.class);
        } catch (final NoClassDefFoundError ex) {
          return false;
        }
      }
    };
    return !Reflection.publicFields(clazz, p).isEmpty();
  }

  public NativeJUnitTestFinder() {
    final Set<MethodFinder> beforeClassFinders = Collections
        .<MethodFinder> singleton(new SimpleAnnotationTestMethodFinder(
            org.junit.BeforeClass.class));

    final Set<MethodFinder> afterClassFinders = Collections
        .<MethodFinder> singleton(new SimpleAnnotationTestMethodFinder(
            org.junit.AfterClass.class));

    final LinkedHashSet<MethodFinder> beforeMethodFinders = new LinkedHashSet<MethodFinder>();
    beforeMethodFinders.add(new SimpleAnnotationTestMethodFinder(
        org.junit.Before.class));
    beforeMethodFinders.add(new JUnit3NameBasedMethodFinder("setUp"));

    final Set<MethodFinder> afterMethodFinders = new LinkedHashSet<MethodFinder>();
    afterMethodFinders.add(new SimpleAnnotationTestMethodFinder(
        org.junit.After.class));
    afterMethodFinders.add(new JUnit3NameBasedMethodFinder("tearDown"));

    final Set<MethodFinder> tmfs = new LinkedHashSet<MethodFinder>();

    tmfs.add(JUnit4TestMethodFinder.instance());
    tmfs.add(JUnit3TestMethodFinder.instance());

    final List<InstantiationStrategy> instantiationStrategies =
    // order is important
    Arrays.<InstantiationStrategy> asList(
        new ParameterizedInstantiationStrategy(),
        new NamedTestSingleStringConstructorInstantiationStrategy(),
        new NoArgsConstructorInstantiationStrategy());

    this.impl = new BasicTestUnitFinder(instantiationStrategies, tmfs,
        beforeMethodFinders, afterMethodFinders, beforeClassFinders,
        afterClassFinders);
  }

  public Collection<TestUnit> findTestUnits(final TestClass clazz,
      final Configuration configuration, final TestDiscoveryListener listener,
      final TestUnitProcessor processor) {
    return this.impl.findTestUnits(clazz, configuration, listener, processor);
  }

}
