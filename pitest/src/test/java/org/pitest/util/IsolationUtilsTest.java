package org.pitest.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pitest.classpath.ClassPath;
import org.pitest.mutationtest.execute.DefaultPITClassloader;

public class IsolationUtilsTest {

  @Test
  public void shouldRecogniseClassFromBootClassLoaderAsFromBootLoader() {
    ClassLoaderDetectionStrategy testee = IsolationUtils
        .loaderDetectionStrategy();
    assertFalse(testee.fromDifferentLoader(Integer.class,
        IsolationUtils.bootClassLoader()));
  }

  @Test
  public void shouldRecogniseClassFromChildOfBootClassLoaderAsFromBootLoader() {
    ClassLoaderDetectionStrategy testee = IsolationUtils
        .loaderDetectionStrategy();
    assertFalse(testee.fromDifferentLoader(IsolationUtilsTest.class,
        IsolationUtils.bootClassLoader()));
  }

  @Test
  public void shouldRecogniseClassFromChildOfChildOfBootClassLoaderAsFromCompatibleLoader()
      throws ClassNotFoundException {
    ClassLoaderDetectionStrategy testee = IsolationUtils
        .loaderDetectionStrategy();
    DefaultPITClassloader parent = new DefaultPITClassloader(new ClassPath(),
        IsolationUtils.bootClassLoader());
    DefaultPITClassloader loader = new DefaultPITClassloader(new ClassPath(),
        parent);

    Class<?> child = loader.loadClass(IsolationUtilsTest.class.getName());

    assertFalse(testee.fromDifferentLoader(child, parent));
  }

  @Test
  public void shouldNotRecogniseClassFromSiblingLoaderAsFromCompatibleLoader()
      throws ClassNotFoundException {
    ClassLoaderDetectionStrategy testee = IsolationUtils
        .loaderDetectionStrategy();
    DefaultPITClassloader siblingLoader = new DefaultPITClassloader(
        new ClassPath(), IsolationUtils.bootClassLoader());
    Class<?> foreign = siblingLoader.loadClass(IsolationUtilsTest.class
        .getName());
    assertTrue(testee.fromDifferentLoader(foreign, new DefaultPITClassloader(
        new ClassPath(), IsolationUtils.bootClassLoader())));
  }
}
