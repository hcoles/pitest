package org.pitest.project.impl;

import java.io.File;

import org.pitest.mutationtest.ReportOptions;
import org.pitest.project.ProjectConfigurationException;
import org.pitest.project.ProjectConfigurationParser;
import org.pitest.project.ProjectConfigurationParserException;

/**
 * @author Aidan Morgan
 */
public class NoDefaultConstructorProjectConfigurationParser implements
    ProjectConfigurationParser {
  public NoDefaultConstructorProjectConfigurationParser(final File f) {

  }

  public ReportOptions loadProject(final String project)
      throws ProjectConfigurationParserException, ProjectConfigurationException {
    return null;
  }
}
