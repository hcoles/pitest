package org.pitest.project;

import org.pitest.mutationtest.ReportOptions;

/**
 * A {@see ProjectFileParser} is responsible for parsing a project file and
 * returning a fully configured {@see ReportOptions} instance which will
 * configure the mutation framework.
 * 
 * @author Aidan Morgan
 */
public interface ProjectConfigurationParser {
  /**
   * Loads the project file from the provided resource, creating a new {@see
   * ReportOptions} instance.
   * 
   * @param project
   *          a string representing the resource to load.
   * @return a new {@see ReportOptions} instance, configured based on the
   *         provided resource.
   */
  public ReportOptions loadProject(String project)
      throws ProjectConfigurationParserException, ProjectConfigurationException;
}
