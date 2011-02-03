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
package org.pitest.junit;

import org.junit.Test;
import org.pitest.TestMethod;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitProcessor;
import org.pitest.extension.common.TimeoutDecorator;
import org.pitest.functional.Option;

public class TimeoutProcessor implements TestUnitProcessor {

  public TestUnit apply(final TestUnit testUnit) {
    final Option<TestMethod> maybeMethod = testUnit.getDescription()
        .getMethod();
    if (maybeMethod.hasSome()) {
      return checkMethodForAnnotation(testUnit);
    } else {
      return testUnit;
    }
  }

  private TestUnit checkMethodForAnnotation(final TestUnit testUnit) {
    final Test annotation = testUnit.getDescription().getMethod().value()
        .getMethod().getAnnotation(Test.class);
    if ((annotation != null) && (annotation.timeout() != 0)) {
      return new TimeoutDecorator(testUnit, annotation.timeout());
    } else {
      return testUnit;
    }
  }

}
