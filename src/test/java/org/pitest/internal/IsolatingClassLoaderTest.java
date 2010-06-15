package org.pitest.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class IsolatingClassLoaderTest {

  IsolatingClassLoader testee;

  @Before
  public void createTestee() {
    this.testee = AccessController
        .doPrivileged(new PrivilegedAction<IsolatingClassLoader>() {
          public IsolatingClassLoader run() {
            return new IsolatingClassLoader();
          }
        });
  }

  @Test(expected = ClassNotFoundException.class)
  public void testThrowsClassNotFoundExceptionForUnknownClass()
      throws ClassNotFoundException {
    this.testee.loadClass("com.not.likely.to.exist.Foo");
  }

  @Test
  public void testCanLoadJavaDotLangClassesWithoutFallingOver()
      throws ClassNotFoundException {
    final Class<?> actual = this.testee.loadClass("java.lang.String");
    assertSame(String.class, actual);
  }

  @Test
  public void testIsolatesClassesDefinedInClassPathInJars()
      throws ClassNotFoundException {
    final Class<?> actual = this.testee.loadClass(Test.class.getName());
    assertNotSame(Test.class, actual);
  }

  @Test
  public void testPitClassesIsolated() throws ClassNotFoundException {
    assertNotSame(IsolatingClassLoaderTest.class, this.testee
        .loadClass(IsolatingClassLoaderTest.class.getName()));

  }

  @Test
  public void testCanLoadResourceAsStream() {
    assertNotNull(this.testee
        .getResourceAsStream(IsolatingClassLoaderTest.class.getName().replace(
            ".", "/")
            + ".class"));
  }

  @Test
  @Ignore
  public void testCanLoadSimpleResource() throws URISyntaxException {
    assertNotNull(this.testee.getResourceAsStream("resourceFolder/text.txt"));
  }

}
