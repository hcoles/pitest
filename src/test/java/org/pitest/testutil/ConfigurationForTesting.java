package org.pitest.testutil;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.pitest.TestMethod;
import org.pitest.extension.Configuration;
import org.pitest.extension.ConfigurationUpdater;
import org.pitest.extension.InstantiationStrategy;
import org.pitest.extension.MethodFinder;
import org.pitest.extension.StaticConfigUpdater;
import org.pitest.extension.TestSuiteFinder;
import org.pitest.extension.TestUnitFinder;
import org.pitest.extension.TestUnitProcessor;
import org.pitest.extension.common.BasicTestUnitFinder;
import org.pitest.extension.common.IgnoreTestProcessor;
import org.pitest.extension.common.NoArgsConstructorInstantiationStrategy;
import org.pitest.extension.common.SimpleAnnotationTestMethodFinder;
import org.pitest.extension.common.testsuitefinder.PITStaticMethodSuiteFinder;
import org.pitest.functional.Option;
import org.pitest.mutationtest.MutationSuiteConfigUpdater;

public class ConfigurationForTesting implements Configuration {

  private static class TestFinder implements MethodFinder {

    public Option<TestMethod> apply(final Method method) {
      final TestAnnotationForTesting annotation = method
          .getAnnotation(TestAnnotationForTesting.class);

      if (annotation != null) {
        final Class<? extends Throwable> expected = !annotation.expected()
            .getName().equals(TestAnnotationForTesting.NONE.class.getName()) ? annotation
            .expected()
            : null;
        return Option.someOrNone(new TestMethod(method, expected));
      } else {
        return Option.none();
      }
    }

  };

  public List<TestUnitProcessor> testUnitProcessors() {
    return Collections
        .<TestUnitProcessor> singletonList(new IgnoreTestProcessor(
            IgnoreAnnotationForTesting.class));
  }

  public List<TestUnitFinder> testUnitFinders() {
    final Set<MethodFinder> beforeClassFinders = Collections
        .<MethodFinder> singleton(new SimpleAnnotationTestMethodFinder(
            BeforeClassAnnotationForTest.class));

    final Set<MethodFinder> afterClassFinders = Collections
        .<MethodFinder> singleton(new SimpleAnnotationTestMethodFinder(
            AfterClassAnnotationForTest.class));

    final Set<MethodFinder> beforeMethodFinders = Collections
        .<MethodFinder> singleton(new SimpleAnnotationTestMethodFinder(
            BeforeAnnotationForTesting.class));

    final Set<MethodFinder> afterMethodFinders = Collections
        .<MethodFinder> singleton(new SimpleAnnotationTestMethodFinder(
            AfterAnnotationForTesting.class));

    final Set<MethodFinder> tmfs = new LinkedHashSet<MethodFinder>();
    tmfs.add(new TestFinder());

    return Collections.<TestUnitFinder> singletonList(new BasicTestUnitFinder(
        tmfs, beforeMethodFinders, afterMethodFinders, beforeClassFinders,
        afterClassFinders));
  }

  public boolean allowConfigurationChange() {
    return true;
  }

  public Collection<TestSuiteFinder> testSuiteFinders() {
    return Arrays.<TestSuiteFinder> asList(new PITStaticMethodSuiteFinder());
  }

  public List<InstantiationStrategy> instantiationStrategies() {
    return Arrays
        .<InstantiationStrategy> asList(new NoArgsConstructorInstantiationStrategy());
  }

  public Collection<ConfigurationUpdater> configurationUpdaters() {
    return Collections
        .<ConfigurationUpdater> singletonList(MutationSuiteConfigUpdater
            .instance());
  }

  public Collection<StaticConfigUpdater> staticConfigurationUpdaters() {
    return Collections.emptyList();
  }

}
