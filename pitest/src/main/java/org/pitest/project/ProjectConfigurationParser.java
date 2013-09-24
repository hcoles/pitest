package org.pitest.project;

import org.pitest.mutationtest.config.ReportOptions;

/**
 * A see ProjectConfigurationParser is responsible for parsing a project file
 * and returning a fully configured ReportOptions instance which will configure
 * the mutation framework.
 * 
 * @author Aidan Morgan
 */
public interface ProjectConfigurationParser {
  /**
   * Loads the project file from the provided resource, creating a new
   * ReportOptions instance.
   * 
   * @param project
   *          a string representing the resource to load.
   * @return a new ReportOptions instance, configured based on the provided
   *         resource.
   */
  public ReportOptions loadProject(String project)
      throws ProjectConfigurationParserException, ProjectConfigurationException;
}
