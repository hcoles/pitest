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
package org.pitest.classinfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class ClassInfoBuilder {

  int                          access;
  ClassIdentifier              id;
  String                       outerClass;
  String                       superClass;
  String                       sourceFile;
  final Set<Integer>           codeLines             = new HashSet<>();
  final Set<String>            annotations           = new HashSet<>(0);
  final Map<ClassName, Object> classAnnotationValues = new HashMap<>(
                                                         0);

  public void registerCodeLine(final int line) {
    this.codeLines.add(line);
  }

  public void registerAnnotation(final String annotation) {
    this.annotations.add(annotation);
  }

  public void registerClassAnnotationValue(final ClassName annotation,
      final Object value) {
    this.classAnnotationValues.put(annotation, value);
  }

}
