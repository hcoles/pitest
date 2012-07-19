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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.pitest.extension.TestSuiteFinder;
import org.pitest.functional.FCollection;
import org.pitest.internal.TestClass;

public class JUnit4SuiteFinder implements TestSuiteFinder {

  public Collection<TestClass> apply(final TestClass a) {
    final SuiteClasses annotation = a.getClazz().getAnnotation(
        SuiteClasses.class);

    if ((annotation != null) && hasSuitableRunnner(a.getClazz())) {
      return FCollection.map(Arrays.asList(annotation.value()),
          TestClass.classToTestClass());
    } else {
      return Collections.emptyList();
    }
  }

  private boolean hasSuitableRunnner(final Class<?> clazz) {

    final RunWith runWith = clazz.getAnnotation(RunWith.class);
    if (runWith != null) {
      return (runWith.value().equals(Suite.class));
    }
    return false;
  }

}
