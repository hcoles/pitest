package org.pitest.project.impl;

import org.pitest.mutationtest.ReportOptions;
import org.pitest.project.ProjectConfigurationException;
import org.pitest.project.ProjectFileParser;
import org.pitest.project.ProjectFileParserException;

import java.io.InputStream;

/**
 * A pretend {@see ProjectFileParser}, used for testing the {@see org.pitest.project.ProjectFileParserFactory}.
 *
 * @author Aidan Morgan
 */
public class FakeProjectFileParser implements ProjectFileParser {
  public ReportOptions loadProjectFile(InputStream stream) throws ProjectFileParserException, ProjectConfigurationException {
    return null;
  }
}
