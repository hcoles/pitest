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
    try {
      this.w.append(IsolationUtils.toXml(this.buffer) + "\n");
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }

    // int start = 0;
    // int end = 0;
    // while (end != this.buffer.size()) {
    // end = start + 600;
    // if (end > this.buffer.size()) {
    // end = this.buffer.size();
    // }
    // try {
    // this.w.append(IsolationUtils.toXml(this.buffer.subList(start, end))
    // + "\n");
    // start = end;
    // } catch (IOException e) {
    // throw Unchecked.translateCheckedException(e);
    // }
    //
    // }
    this.buffer.clear();

  }

}
