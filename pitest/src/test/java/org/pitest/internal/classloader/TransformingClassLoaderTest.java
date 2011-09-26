package org.pitest.internal.classloader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.junit.Before;
import org.junit.Test;
import org.pitest.extension.common.ExcludedPrefixIsolationStrategy;
import org.pitest.internal.ClassPath;
import org.pitest.internal.isolation.IsolatedSystem;
import org.pitest.internal.transformation.EnvironmentAccessTransformation;

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
  public void shouldThrowClassNotFoundExceptionForUnknownClass()
      throws ClassNotFoundException {
    this.testee.loadClass("com.not.likely.to.exist.Foo");
  }

  @Test
  public void shouldLoadJavaDotLangClassesWithoutFallingOver()
      throws ClassNotFoundException {
    final Class<?> actual = this.testee.loadClass("java.lang.String");
    assertSame(String.class, actual);
  }

  @Test
  public void shouldIsolateClassesDefinedInClassPathInJars()
      throws ClassNotFoundException {
    final Class<?> actual = this.testee.loadClass(Test.class.getName());
    assertNotSame(Test.class, actual);
  }

  @Test
  public void shouldIsolatePitClasses() throws ClassNotFoundException {
    assertNotSame(TransformingClassLoaderTest.class,
        this.testee.loadClass(TransformingClassLoaderTest.class.getName()));

  }

  @Test
  public void shouldLoadSimpleResource() throws URISyntaxException {
    assertNotNull(this.testee.getResourceAsStream("text.txt"));
  }

  @Test
  public void shouldLoadResourceWithSpecialCharsInName()
      throws URISyntaxException {
    assertNotNull(this.testee.getResourceAsStream("! awkward name ~.txt"));
  }

  @Test
  public void shouldLoadResourceWithSpaceInFolderName()
      throws URISyntaxException {
    assertNotNull(this.testee.getResource("resource folder with spaces"));
    assertNotNull(this.testee
        .getResource("resource folder with spaces/text in folder with spaces.txt"));
  }

  @Test
  public void shouldNotThrowExceptionIfGivenNonExistantRoot()
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
  public void shouldDefinePackages() throws Exception {
    final Class<?> c = this.testee.loadClass(TransformingClassLoaderTest.class
        .getName());
    assertNotNull(c.getPackage());
  }

}
