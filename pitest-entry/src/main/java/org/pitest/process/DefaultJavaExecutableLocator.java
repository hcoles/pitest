package org.pitest.process;

/**
 * Returns the java binary from JAVA_HOME
 */
public class DefaultJavaExecutableLocator implements JavaExecutableLocator {

  @Override
  public String javaExecutable() {
    final String separator = System.getProperty("file.separator");
    return System.getProperty("java.home") + separator + "bin" + separator
        + "java";
  }

}
