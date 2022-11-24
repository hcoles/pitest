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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pitest.coverage.codeassist.ClassUtils;

public class ClassInfoVisitorTest {
  @Test
  public void shouldRecordSourceFile() throws ClassNotFoundException {
    final String sampleName = String.class.getName();
    final ClassInfoBuilder actual = getClassInfo(sampleName,
        ClassUtils.classAsBytes(sampleName));
    assertEquals("String.java", actual.sourceFile);
  }

  private ClassInfoBuilder getClassInfo(final String name, final byte[] bytes) {
    return ClassInfoVisitor.getClassInfo(ClassName.fromString(name), bytes, 0);
  }

}
