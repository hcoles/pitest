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
package org.pitest.coverage;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.pitest.classinfo.ClassName;

public class ClassStatistics implements Serializable {

  private static final long  serialVersionUID = 1L;

  private final ClassName    className;
  private final Set<Integer> visitedLines     = new HashSet<Integer>(0);

  public ClassStatistics(final String className) {
    this(ClassName.fromString(className));
  }

  public ClassStatistics(final ClassName className) {
    this.className = className;
  }

  public ClassName getClassName() {
    return this.className;
  }

  public boolean wasVisited() {
    return !this.visitedLines.isEmpty();
  }

  public Set<Integer> getUniqueVisitedLines() {
    return this.visitedLines;
  }

  public synchronized void registerLineVisit(final int lineId) {
    this.visitedLines.add(lineId);
  }

  @Override
  public String toString() {
    return "ClassStatistics [className=" + this.className + ", visitedLines="
        + this.visitedLines + "]";
  }
}
