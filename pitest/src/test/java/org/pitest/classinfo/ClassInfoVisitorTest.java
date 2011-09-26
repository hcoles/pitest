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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pitest.coverage.codeassist.ClassUtils;
import org.pitest.coverage.codeassist.samples.Bridge.HasBridgeMethod;
import org.pitest.coverage.codeassist.samples.HasDefaultConstructor;
import org.pitest.coverage.codeassist.samples.NoDefaultConstructor;

public class ClassInfoVisitorTest {

  @Test
  public void shouldDetectStandardCodeLines() throws Exception {
    final String sampleName = NoDefaultConstructor.class.getName();
    final ClassInfo actual = ClassInfoVisitor.getClassInfo(sampleName,
        ClassUtils.classAsBytes(sampleName));

    assertTrue(actual.isCodeLine(25));
  }

  @Test
  public void shouldDetectCodeLineAtClassDeclarationsWhenClassHasDefaultConstructor()
      throws Exception {
    final String sampleName = HasDefaultConstructor.class.getName();
    final ClassInfo actual = ClassInfoVisitor.getClassInfo(sampleName,
        ClassUtils.classAsBytes(sampleName));
    assertTrue(
        "first line of class with default constructor should be a code line",
        actual.isCodeLine(17));
    assertFalse("line before should not be a code line", actual.isCodeLine(16));
  }

  @Test
  public void shouldNotDetectCodeLineAtClassDeclarationsWhenClassHasNoDefaultConstructor()
      throws Exception {
    final String sampleName = NoDefaultConstructor.class.getName();
    final ClassInfo actual = ClassInfoVisitor.getClassInfo(sampleName,
        ClassUtils.classAsBytes(sampleName));
    assertFalse(
        "first line of class without default constructor should not be a code line",
        actual.isCodeLine(17));
  }

  @Test
  public void shouldNotRecordLineNumbersFromSyntheticBridgeMethods()
      throws Exception {
    final String sampleName = HasBridgeMethod.class.getName();
    final ClassInfo actual = ClassInfoVisitor.getClassInfo(sampleName,
        ClassUtils.classAsBytes(sampleName));
    assertFalse(actual.isCodeLine(1));
  }

}
