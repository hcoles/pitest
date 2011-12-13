package org.pitest.project;

/**
 * An {@see Exception} that is thrown if there is a problem parsing a project
 * file.
 * 
 * @author Aidan Morgan
 */
public class ProjectConfigurationParserException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * @inheritDoc
   */
  public ProjectConfigurationParserException() {
    super();
  }

  /**
   * @inheritDoc
   */
  public ProjectConfigurationParserException(final String message) {
    super(message);
  }

  /**
   * @inheritDoc
   */
  public ProjectConfigurationParserException(final String message,
      final Throwable cause) {
    super(message, cause);
  }

  /**
   * @inheritDoc
   */
  public ProjectConfigurationParserException(final Throwable cause) {
    super(cause);
  }
}
