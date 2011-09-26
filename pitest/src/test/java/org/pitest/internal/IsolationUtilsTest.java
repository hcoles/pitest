package org.pitest.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pitest.dependency.DependencyExtractorTest.Foo;
import org.pitest.internal.classloader.DefaultPITClassloader;

public class IsolationUtilsTest {

  @Test
  public void shouldSerializeAndDeserializeForTransport() {

    final String encodedXml = IsolationUtils.toTransportString(new ClassPath());
    IsolationUtils.fromTransportString(encodedXml);
    // pass if get here without error
  }

  @Test
  public void loaderAgnosticInstanceOfShouldReturnFalseForDifferentTypes() {
    assertFalse(IsolationUtils.loaderAgnosticInstanceOf("foo", Integer.class));
  }

  @Test
  public void loaderAgnosticInstanceOfShouldReturnTrueForSameTypeFromSameLoader() {
    assertTrue(IsolationUtils.loaderAgnosticInstanceOf(1, Integer.class));
  }

  @Test
  public void loaderAgnosticInstanceOfShouldReturnTrueForSameTypeFromDifferentLoaders() {
    final ClassLoader loader = new DefaultPITClassloader(new ClassPath(),
        IsolationUtils.getContextClassLoader());
    final Object foo = IsolationUtils.cloneForLoader(new Foo(), loader);
    assertTrue(IsolationUtils.loaderAgnosticInstanceOf(foo, Foo.class));
  }
}
