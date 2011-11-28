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

package org.pitest.extension.common;

import static org.pitest.util.Functions.hasAnnotation;

import java.lang.annotation.Annotation;

import org.pitest.TestMethod;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitProcessor;
import org.pitest.testunit.IgnoredTestUnit;

public class IgnoreTestProcessor implements TestUnitProcessor {

  private static final long         serialVersionUID = 1L;
  private final Class<? extends Annotation> annotationType;

  public IgnoreTestProcessor(final Class<? extends Annotation> annotation) {
    this.annotationType = annotation;
  }

  public TestUnit apply(final TestUnit tu) {
    if (hasClassLevelIgnoreAnnotation(tu) || hasMethodLevelIgnoreAnnotation(tu)) {
      return new IgnoredTestUnit(tu);
    } else {
      return tu;
    }
  }

  private boolean hasMethodLevelIgnoreAnnotation(final TestUnit tu) {
    for (final TestMethod each : tu.getDescription().getMethod()) {
      return (each.getMethod().getAnnotation(this.annotationType) != null);
    }
    return false;
  }

  private boolean hasClassLevelIgnoreAnnotation(final TestUnit tu) {
    return tu.getDescription().contains(
        hasAnnotation(IgnoreTestProcessor.this.annotationType));

  }

}
