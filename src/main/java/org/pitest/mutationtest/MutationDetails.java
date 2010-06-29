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
package org.pitest.mutationtest;

import java.io.Serializable;

public class MutationDetails implements Serializable {

  private static final long       serialVersionUID = 1L;

  private final StackTraceElement stackTraceElement;
  private final String            description;

  public MutationDetails(final String clazz, final String filename,
      final String description, final String method) {
    this.description = description;
    this.stackTraceElement = new StackTraceElement(clazz, method, filename,
        parseLineNumber(description));
  }

  private int parseLineNumber(final String description) {
    final int start = description.indexOf(":") + 1;
    final int end = description.indexOf(":", start);
    return Integer.parseInt(description.substring(start, end));
  }

  public StackTraceElement stackTraceDescription() {
    return this.stackTraceElement;
  }

  @Override
  public String toString() {
    return this.description;
  }

}
