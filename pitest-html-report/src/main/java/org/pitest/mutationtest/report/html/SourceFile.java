/*
 * Copyright 2010 Henry Coles
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
package org.pitest.mutationtest.report.html;

import java.util.List;

public class SourceFile {

  private final String                 fileName;
  private final List<Line>             lines;
  private final List<MutationGrouping> groups;

  public SourceFile(final String fileName, final List<Line> lines,
      final List<MutationGrouping> groups) {
    this.fileName = fileName;
    this.lines = lines;
    this.groups = groups;
  }

  public String getFileName() {
    return this.fileName;
  }

  public List<Line> getLines() {
    return this.lines;
  }

  public List<MutationGrouping> getGroups() {
    return this.groups;
  }

}
