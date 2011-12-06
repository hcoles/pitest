package org.pitest.project;

/**
 * An {@see Exception} that is thrown if there is a problem parsing a project file.
 *
 * @author Aidan Morgan
 */
public class ProjectFileParserException extends Exception {
  /**
   * @inheritDoc
   */
  public ProjectFileParserException() {
    super();
  }

  /**
   * @inheritDoc
   */
  public ProjectFileParserException(String message) {
    super(message);
  }

  /**
   * @inheritDoc
   */
  public ProjectFileParserException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @inheritDoc
   */
  public ProjectFileParserException(Throwable cause) {
    super(cause);
  }
}
