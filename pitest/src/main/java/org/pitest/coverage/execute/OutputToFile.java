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

package org.pitest.coverage.execute;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.pitest.functional.FCollection;
import org.pitest.functional.SideEffect1;
import org.pitest.internal.IsolationUtils;
import org.pitest.util.Unchecked;

public class OutputToFile implements SideEffect1<CoverageResult> {

  private final static int           BUFFER_SIZE = 100;

  private final List<CoverageResult> buffer      = new ArrayList<CoverageResult>(
                                                     BUFFER_SIZE);

  private final Writer               w;

  public OutputToFile(final Writer w) {
    this.w = w;
  }

  public void apply(final CoverageResult a) {
    this.buffer.add(a);

    if (this.buffer.size() >= BUFFER_SIZE) {
      writeToDisk();
    }

  }

  void writeToDisk() {

    // this.w.append(IsolationUtils.toXml(this.buffer) + "\n");
    FCollection.forEach(this.buffer, writeToFile());

    this.buffer.clear();

  }

  private SideEffect1<CoverageResult> writeToFile() {
    return new SideEffect1<CoverageResult>() {

      public void apply(final CoverageResult a) {
        try {
          OutputToFile.this.w.append(IsolationUtils.toXml(a) + "\n");
        } catch (final IOException e) {
          throw Unchecked.translateCheckedException(e);
        }
      }

    };
  }

}
