package org.pitest.project;

import org.pitest.mutationtest.ReportOptions;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;

/**
 * A {@see ProjectFileParser} is responsible for parsing a project file and returning a fully configured
 * {@see ReportOptions} instance which will configure the mutation framework.
 *
 * @author Aidan Morgan
 */
public interface ProjectFileParser {
  /**
   * Loads the project file from the provided {@see File}, creating a new {@see ReportOptions} instance.
   *
   * @param projectFile the {@see File} to load the {@see ReportOptions} from.
   * @return a new {@see ReportOptions} instance, configured based on the provided {@see File}.
   */
  public ReportOptions loadProjectFile(File projectFile) throws ProjectFileParserException, ProjectConfigurationException;
}
