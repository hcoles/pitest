package org.pitest.project.impl;

import org.pitest.mutationtest.ReportOptions;
import org.pitest.project.ProjectConfigurationException;
import org.pitest.project.ProjectConfigurationParser;
import org.pitest.project.ProjectConfigurationParserException;

import java.io.File;

/**
 * @author Aidan Morgan
 */
public class NoDefaultConstructorProjectConfigurationParser implements ProjectConfigurationParser {
  public NoDefaultConstructorProjectConfigurationParser(File f) {

  }

  public ReportOptions loadProject(String project) throws ProjectConfigurationParserException, ProjectConfigurationException {
    return null;
  }
}
