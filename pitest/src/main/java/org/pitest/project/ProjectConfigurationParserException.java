package org.pitest.project;

/**
 * An Exception that is thrown if there is a problem parsing a project file.
 * 
 * @author Aidan Morgan
 */
public class ProjectConfigurationParserException extends Exception {
  private static final long serialVersionUID = 1L;

  public ProjectConfigurationParserException() {
    super();
  }

  public ProjectConfigurationParserException(final String message) {
    super(message);
  }

  public ProjectConfigurationParserException(final Throwable cause) {
    super(cause);
  }
}
