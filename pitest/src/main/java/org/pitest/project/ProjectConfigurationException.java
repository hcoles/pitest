package org.pitest.project;

/**
 * An Exception that is thrown in the case of a configuration issue with a
 * project file.
 * 
 * @author Aidan Morgan
 */
public class ProjectConfigurationException extends Exception {
  private static final long serialVersionUID = 1L;

  public ProjectConfigurationException() {
    super();
  }

  public ProjectConfigurationException(final String message) {
    super(message);
  }

}
