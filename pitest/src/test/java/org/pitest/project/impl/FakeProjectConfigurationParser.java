package org.pitest.project.impl;

import org.pitest.mutationtest.ReportOptions;
import org.pitest.project.ProjectConfigurationException;
import org.pitest.project.ProjectConfigurationParser;
import org.pitest.project.ProjectConfigurationParserException;

import java.io.InputStream;

/**
 * A pretend {@see ProjectFileParser}, used for testing the {@see org.pitest.project.ProjectFileParserFactory}.
 *
 * @author Aidan Morgan
 */
public class FakeProjectConfigurationParser implements ProjectConfigurationParser {
  public ReportOptions loadProject(String project) throws ProjectConfigurationParserException, ProjectConfigurationException {
    return null;
  }
}
