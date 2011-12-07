package org.pitest.project;

import org.pitest.mutationtest.ReportOptions;

import java.io.InputStream;

/**
 * A {@see ProjectFileParser} is responsible for parsing a project file and returning a fully configured
 * {@see ReportOptions} instance which will configure the mutation framework.
 *
 * @author Aidan Morgan
 */
public interface ProjectFileParser {
  /**
   * Loads the project file from the provided {@see InputStream}, creating a new {@see ReportOptions} instance.
   *
   * @param stream the {@see InputStream} to load the {@see ReportOptions} from.
   * @return a new {@see ReportOptions} instance, configured based on the provided {@see InputStream}.
   */
  public ReportOptions loadProjectFile(InputStream stream) throws ProjectFileParserException, ProjectConfigurationException;
}
