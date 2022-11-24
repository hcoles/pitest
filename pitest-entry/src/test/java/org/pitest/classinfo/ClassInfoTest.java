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

import org.junit.Before;
import org.junit.Test;
import java.util.Optional;

public class ClassInfoTest {

  private ClassInfoBuilder data;

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

  private ClassPointer emptyClassPointer() {
    return () -> Optional.empty();
  }

  private ClassPointer pointerTo(final ClassInfo ci) {
    return () -> Optional.ofNullable(ci);
  }

}
