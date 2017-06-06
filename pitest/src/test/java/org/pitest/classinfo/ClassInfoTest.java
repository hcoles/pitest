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
import org.objectweb.asm.Opcodes;
import org.pitest.functional.Option;

public class ClassInfoTest {

  private ClassInfoBuilder data;
  private ClassInfo        testee;

  @Before
  public void setUp() {
    this.data = new ClassInfoBuilder();
    this.data.id = new ClassIdentifier(1, ClassName.fromString("foo"));
  }

  @Test
  public void shouldCreateDifferentHierarchicalHashWhenParentClassChanges() {
    final ClassInfo parent = new ClassInfo(emptyClassPointer(),
        emptyClassPointer(), this.data);
    final ClassInfo classA = new ClassInfo(emptyClassPointer(),
        emptyClassPointer(), this.data);
    final ClassInfo classB = new ClassInfo(pointerTo(parent),
        emptyClassPointer(), this.data);

    assertFalse(classA.getHierarchicalId().getHierarchicalHash()
        .equals(classB.getHierarchicalId().getHierarchicalHash()));
  }

  @Test
  public void shouldCreateDifferentHierarchicalHashWhenOuterClassChanges() {
    final ClassInfo outer = new ClassInfo(emptyClassPointer(),
        emptyClassPointer(), this.data);
    final ClassInfo classA = new ClassInfo(emptyClassPointer(),
        emptyClassPointer(), this.data);
    final ClassInfo classB = new ClassInfo(emptyClassPointer(),
        pointerTo(outer), this.data);

    assertFalse(classA.getHierarchicalId().getHierarchicalHash()
        .equals(classB.getHierarchicalId().getHierarchicalHash()));
  }

  @Test
  public void shouldReportWhenClassIsSynthetic() {
    this.data.access = Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PUBLIC;
    final ClassInfo testee = new ClassInfo(emptyClassPointer(),
        emptyClassPointer(), this.data);
    assertTrue(testee.isSynthetic());
  }
  
  @Test
  public void shouldReportWhenClassIsNotSynthetic() {
    this.data.access = Opcodes.ACC_PUBLIC;
    final ClassInfo testee = new ClassInfo(emptyClassPointer(),
        emptyClassPointer(), this.data);
    assertFalse(testee.isSynthetic());
  }
  
  private ClassPointer emptyClassPointer() {
    return new ClassPointer() {
      @Override
      public Option<ClassInfo> fetch() {
        return Option.none();
      }

    };
  }

  private ClassPointer pointerTo(final ClassInfo ci) {
    return new ClassPointer() {
      @Override
      public Option<ClassInfo> fetch() {
        return Option.some(ci);
      }

    };
  }

  @Test
  public void isCodeLineReturnShouldTrueForCodeLines() {
    final List<Integer> codeLines = Arrays.asList(1, 2, 3, 4, 5, 6, 10);
    addCodeLines(codeLines);
    makeTestee();
    for (final int each : codeLines) {
      assertTrue(this.testee.isCodeLine(each));
    }
  }

  @Test
  public void isCodeLineShouldReturnFalseForNonCodeLines() {
    final List<Integer> codeLines = Arrays.asList(1);
    addCodeLines(codeLines);
    makeTestee();
    assertFalse(this.testee.isCodeLine(2));
  }

  @Test
  public void matchIfAbstractShouldReturnTrueForAbstractClasses() {
    this.data.access = Opcodes.ACC_ABSTRACT;
    makeTestee();
    assertTrue(ClassInfo.matchIfAbstract().apply(this.testee));
  }

  private void makeTestee() {
    this.testee = new ClassInfo(null, null, this.data);
  }

  private void addCodeLines(final List<Integer> lines) {
    for (final int each : lines) {
      this.data.registerCodeLine(each);
    }
  }

}
