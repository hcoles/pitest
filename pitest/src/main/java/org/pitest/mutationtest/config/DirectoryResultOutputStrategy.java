/*
 * Copyright 2011 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.pitest.util.ResultOutputStrategy;
import org.pitest.util.Unchecked;

public class DirectoryResultOutputStrategy implements ResultOutputStrategy {

  private final File reportDir;

  public DirectoryResultOutputStrategy(final String baseDir,
      final ReportDirCreationStrategy dirCreationStrategy) {
    this.reportDir = dirCreationStrategy.createReportDir(baseDir);
  }

  @Override
  public Writer createWriterForFile(final String file) {
    try {
      final int fileSepIndex = file.lastIndexOf(File.separatorChar);
      if (fileSepIndex > 0) {
        final String directory = this.reportDir.getAbsolutePath()
            + File.separatorChar + file.substring(0, fileSepIndex);
        final File directoryFile = new File(directory);
        if (!directoryFile.exists()) {
          directoryFile.mkdirs();
        }
      }
      return new BufferedWriter(new FileWriter(this.reportDir.getAbsolutePath()
          + File.separatorChar + file));
    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

}