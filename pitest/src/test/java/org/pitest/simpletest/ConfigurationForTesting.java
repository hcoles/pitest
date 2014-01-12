package org.pitest.simpletest;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.pitest.classinfo.ClassInfo;
import org.pitest.extension.common.NoTestSuiteFinder;
import org.pitest.functional.Option;
import org.pitest.help.PitHelpError;
import org.pitest.junit.CompoundTestUnitFinder;
import org.pitest.testapi.*;

public class ConfigurationForTesting implements Configuration {

  private static class TestFinder implements MethodFinder {

    public Option<TestMethod> apply(final Method method) {
      final TestAnnotationForTesting annotation = method
          .getAnnotation(TestAnnotationForTesting.class);

      if (annotation != null) {
        final Class<? extends Throwable> expected = !annotation.expected()
            .getName().equals(TestAnnotationForTesting.NONE.class.getName()) ? annotation
            .expected() : null;
        return Option.some(new TestMethod(method, expected));
      } else {
        return Option.none();
      }
    }

  }

  public TestUnitFinder testUnitFinder() {

    final Set<MethodFinder> tmfs = new LinkedHashSet<MethodFinder>();
    tmfs.add(new TestFinder());

    final List<InstantiationStrategy> instantiationStrategies =

    Arrays
        .<InstantiationStrategy> asList(new NoArgsConstructorInstantiationStrategy());

    return new CompoundTestUnitFinder(
        Collections.<TestUnitFinder> singletonList(new BasicTestUnitFinder(
            instantiationStrategies, tmfs)));
  }

  public TestSuiteFinder testSuiteFinder() {
    return new NoTestSuiteFinder();
  }

  public TestClassIdentifier testClassIdentifier() {
    return new BaseTestClassIdentifier() {

      public boolean isATestClass(final ClassInfo a) {
        return a.hasAnnotation(TestAnnotationForTesting.class);
      }
    };
  }

  public Option<PitHelpError> verifyEnvironment() {
    return Option.none();
  }

}
