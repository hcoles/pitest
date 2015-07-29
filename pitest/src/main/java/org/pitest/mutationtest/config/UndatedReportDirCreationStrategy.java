package org.pitest.mutationtest.config;

import java.io.File;

public class UndatedReportDirCreationStrategy implements
ReportDirCreationStrategy {

  @Override
  public File createReportDir(final String base) {
    final File reportDir = new File(base);
    reportDir.mkdirs();
    return reportDir;
  }

}
