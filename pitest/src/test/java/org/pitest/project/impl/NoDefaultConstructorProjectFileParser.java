package org.pitest.project.impl;

import org.pitest.mutationtest.ReportOptions;
import org.pitest.project.ProjectConfigurationException;
import org.pitest.project.ProjectFileParser;
import org.pitest.project.ProjectFileParserException;

import java.io.File;
import java.io.InputStream;

/**
 * @author Aidan Morgan
 */
public class NoDefaultConstructorProjectFileParser implements ProjectFileParser {
  public NoDefaultConstructorProjectFileParser(File f) {

  }

  public ReportOptions loadProjectFile(InputStream stream) throws ProjectFileParserException, ProjectConfigurationException {
    return null;
  }
}
