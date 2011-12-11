package org.pitest.project;

/**
 * An {@see Exception} that is thrown if there is a problem parsing a project file.
 *
 * @author Aidan Morgan
 */
public class ProjectConfigurationParserException extends Exception {
  /**
   * @inheritDoc
   */
  public ProjectConfigurationParserException() {
    super();
  }

  /**
   * @inheritDoc
   */
  public ProjectConfigurationParserException(String message) {
    super(message);
  }

  /**
   * @inheritDoc
   */
  public ProjectConfigurationParserException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @inheritDoc
   */
  public ProjectConfigurationParserException(Throwable cause) {
    super(cause);
  }
}
