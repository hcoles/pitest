package org.pitest.internal.classloader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.junit.Before;
import org.junit.Test;
import org.pitest.extension.common.ExcludedPrefixIsolationStrategy;
import org.pitest.internal.ClassPath;
import org.pitest.internal.transformation.EnvironmentAccessTransformation;
import org.pitest.internal.transformation.IsolatedSystem;

public class TransformingClassLoaderTest {

  private TransformingClassLoader testee;

  @Before
  public void setUp() {
    this.testee = AccessController
        .doPrivileged(new PrivilegedAction<TransformingClassLoader>() {
          public TransformingClassLoader run() {
            return new TransformingClassLoader(
                new EnvironmentAccessTransformation(),
                new ExcludedPrefixIsolationStrategy(IsolatedSystem.class
                    .getName()));
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
    assertNotSame(TransformingClassLoaderTest.class, this.testee
        .loadClass(TransformingClassLoaderTest.class.getName()));

  }

  @Test
  public void testCanLoadSimpleResource() throws URISyntaxException {
    assertNotNull(this.testee.getResourceAsStream("resourceFolder/text.txt"));
  }

  @Test
  public void testCanLoadResourceWithSpecialCharsInName()
      throws URISyntaxException {
    assertNotNull(this.testee
        .getResourceAsStream("resource folder with spaces/! awkward name ~.txt"));
  }

  @Test
  public void testCanLoadResourceWithSpaceInFolderName()
      throws URISyntaxException {
    final URL url = this.testee.getResource("resource folder with spaces");
    assertNotNull(url);
    url.toURI();
  }

  @Test
  public void testDoesNotThrowExceptionIfGivenNonExistantRoot()
      throws ClassNotFoundException {
    final ClassPath cp = new ClassPath();
    cp.addRoot(new DirectoryClassPathRoot(new File("foo")));

    this.testee = new TransformingClassLoader(cp,
        new EnvironmentAccessTransformation(),
        new ExcludedPrefixIsolationStrategy(IsolatedSystem.class.getName()),
        null);
    try {
      this.testee.loadClass(TransformingClassLoaderTest.class.getName());
    } catch (final Exception ex) {
      fail();
    }

  }

  @Test
  public void testDefinesPackages() throws Exception {
    final Class<?> c = this.testee.loadClass(TransformingClassLoaderTest.class
        .getName());
    assertNotNull(c.getPackage());
  }

}
