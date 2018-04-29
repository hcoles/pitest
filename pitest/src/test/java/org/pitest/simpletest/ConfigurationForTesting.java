package org.pitest.simpletest;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.pitest.extension.common.NoTestSuiteFinder;
import org.pitest.help.PitHelpError;
import org.pitest.junit.CompoundTestUnitFinder;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestSuiteFinder;
import org.pitest.testapi.TestUnitFinder;

public class ConfigurationForTesting implements Configuration {

  private static class TestFinder implements MethodFinder {

    @Override
    public Optional<TestMethod> apply(final Method method) {
      final TestAnnotationForTesting annotation = method
          .getAnnotation(TestAnnotationForTesting.class);

      if (annotation != null) {
        final Class<? extends Throwable> expected = !annotation.expected()
            .getName().equals(TestAnnotationForTesting.NONE.class.getName()) ? annotation
                .expected() : null;
                return Optional.ofNullable(new TestMethod(method, expected));
      } else {
        return Optional.empty();
      }
    }

  }

  @Override
  public TestUnitFinder testUnitFinder() {
    final List<InstantiationStrategy> instantiationStrategies =

        Arrays
        .<InstantiationStrategy> asList(new NoArgsConstructorInstantiationStrategy());

    return new CompoundTestUnitFinder(
        Collections.<TestUnitFinder> singletonList(new BasicTestUnitFinder(
            instantiationStrategies, new TestFinder())));
  }

  @Override
  public TestSuiteFinder testSuiteFinder() {
    return new NoTestSuiteFinder();
  }

  @Override
  public Optional<PitHelpError> verifyEnvironment() {
    return Optional.empty();
  }

}
