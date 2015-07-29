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
package org.pitest.dependency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.pitest.classpath.ClassPath;
import org.pitest.dependency.DependencyAccess.Member;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.SideEffect1;

public class DependencyClassVisitorTest {

  private DependencyClassVisitor      testee;
  private final Set<String>           gatheredDependencies = new HashSet<String>();
  private final Set<DependencyAccess> gatheredAccess       = new HashSet<DependencyAccess>();
  private final ClassPath             cp                   = new ClassPath();

  @Before
  public void setUp() {
    final SideEffect1<DependencyAccess> se = new SideEffect1<DependencyAccess>() {
      @Override
      public void apply(final DependencyAccess a) {
        DependencyClassVisitorTest.this.gatheredAccess.add(a);
        DependencyClassVisitorTest.this.gatheredDependencies.add(a.getDest()
            .getOwner());
      }

    };
    this.testee = new DependencyClassVisitor(new ClassWriter(0), se);
  }

  public static class HasDependencyFromCallingNew {
    public void foo() {
      new Integer(1);
    }
  }

  @Test
  public void shouldRecordDirectDependenciesFromCallingNew() throws Exception {
    examineClassWithTestee(HasDependencyFromCallingNew.class);
    assertEquals(classesToNames(Integer.class), this.gatheredDependencies);
  }

  public enum AnEnum {
    value
  }

  public static class HasField {
    AnEnum field = AnEnum.value;

  }

  @Test
  public void shouldRecordDependenciesFromInitializedFields() throws Exception {
    examineClassWithTestee(HasField.class);
    assertEquals(classesToNames(AnEnum.class, HasField.class),
        this.gatheredDependencies);

  }

  public static class MakesMethodCall {
    public void foo() {
      Arrays.asList();
    }
  }

  @Test
  public void shouldRecordDependenciesFromMethodCalls() throws Exception {
    examineClassWithTestee(MakesMethodCall.class);
    assertEquals(classesToNames(Arrays.class), this.gatheredDependencies);
    final Member foo = new Member(
        classToJvmName().apply(MakesMethodCall.class), "foo");
    assertTrue(this.gatheredAccess.contains(new DependencyAccess(foo,
        new Member("java/util/Arrays", "asList"))));
  }

  private void examineClassWithTestee(final Class<?> clazz) throws IOException {
    final byte[] bytes = this.cp.getClassData(clazz.getName());
    final ClassReader reader = new ClassReader(bytes);
    reader.accept(this.testee, 0);
  }

  private Set<String> classesToNames(final Class<?>... classes) {
    final Set<String> set = new HashSet<String>();
    FCollection.mapTo(Arrays.asList(classes), classToJvmName(), set);
    return set;
  }

  private F<Class<?>, String> classToJvmName() {
    return new F<Class<?>, String>() {
      @Override
      public String apply(final Class<?> a) {
        return a.getName().replace(".", "/");
      }

    };
  }

}
