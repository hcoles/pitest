package org.pitest.project;

/**
 * An {@see Exception} that is thrown in the case of a configuration issue with a project file.
 *
 * @author Aidan Morgan
 */
public class ProjectConfigurationException extends Exception {
  /**
   * @inheritDoc
   */
  public ProjectConfigurationException() {
    super();
  }

  /**
   * @inheritDoc
   */
  public ProjectConfigurationException(String message) {
    super(message);
  }

  /**
   * @inheritDoc
   */
  public ProjectConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @inheritDoc
   */
  public ProjectConfigurationException(Throwable cause) {
    super(cause);
  }
}
