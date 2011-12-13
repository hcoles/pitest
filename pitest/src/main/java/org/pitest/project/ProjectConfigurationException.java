package org.pitest.project;

/**
 * An {@see Exception} that is thrown in the case of a configuration issue with
 * a project file.
 * 
 * @author Aidan Morgan
 */
public class ProjectConfigurationException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * @inheritDoc
   */
  public ProjectConfigurationException() {
    super();
  }

  /**
   * @inheritDoc
   */
  public ProjectConfigurationException(final String message) {
    super(message);
  }

  /**
   * @inheritDoc
   */
  public ProjectConfigurationException(final String message,
      final Throwable cause) {
    super(message, cause);
  }

  /**
   * @inheritDoc
   */
  public ProjectConfigurationException(final Throwable cause) {
    super(cause);
  }
}
