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

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ClassInfoTest {

  private ClassInfo testee;

  @Before
  public void setUp() {
    this.testee = new ClassInfo("foo");
  }

  @Test
  public void testIsCodeLineReturnsTrueForCodeLines() {
    final List<Integer> codeLines = Arrays.asList(1, 2, 3, 4, 5, 6, 10);
    addCodeLines(codeLines);
    for (final int each : codeLines) {
      assertTrue(this.testee.isCodeLine(each));
    }
  }

  @Test
  public void testIsCodeLineReturnsFalseForNonCodeLines() {
    final List<Integer> codeLines = Arrays.asList(1);
    addCodeLines(codeLines);
    assertFalse(this.testee.isCodeLine(2));
  }

  private void addCodeLines(final List<Integer> lines) {
    for (final int each : lines) {
      this.testee.registerCodeLine(each);
    }
  }

}
