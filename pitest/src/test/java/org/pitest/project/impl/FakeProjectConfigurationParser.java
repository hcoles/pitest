package org.pitest.project.impl;

import org.pitest.mutationtest.ReportOptions;
import org.pitest.project.ProjectConfigurationException;
import org.pitest.project.ProjectConfigurationParser;
import org.pitest.project.ProjectConfigurationParserException;

/**
 * A pretend ProjectFileParser, used for testing the
 * org.pitest.project.ProjectFileParserFactory.
 * 
 * @author Aidan Morgan
 */
public class FakeProjectConfigurationParser implements
    ProjectConfigurationParser {
  public ReportOptions loadProject(final String project)
      throws ProjectConfigurationParserException, ProjectConfigurationException {
    return null;
  }
}
