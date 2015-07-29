/*
 * Copyright 2011 Henry Coles
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
package org.pitest.testng;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.pitest.classinfo.Repository;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.util.IsolationUtils;

import com.example.testng.AnnotatedAtClassLevel;
import com.example.testng.InheritsFromAnnotatedBase;

public class TestNGTestClassIdentifierTest {

  private TestNGTestClassIdentifier testee;
  private Repository                classRepostory;

  @Before
  public void setUp() {
    this.testee = new TestNGTestClassIdentifier();
    this.classRepostory = new Repository(new ClassloaderByteArraySource(
        IsolationUtils.getContextClassLoader()));
  }

  @Test
  public void shouldRecogniseClassLevelTestAnnotations() {
    assertTrue(this.testee.isATestClass(this.classRepostory.fetchClass(
        AnnotatedAtClassLevel.class).value()));
  }

  @Test
  public void shouldRecogniseMethodLevelTestAnnotations() {
    assertTrue(this.testee.isATestClass(this.classRepostory.fetchClass(
        com.example.testng.AnnotatedAtMethodLevel.class).value()));
  }

  @Test
  public void shouldNotRecogniseUnAnnotatedClassesAsTests() {
    assertFalse(this.testee.isATestClass(this.classRepostory.fetchClass(
        String.class).value()));
  }

  @Test
  public void shouldRecogniseUnannotaedClassesWithAnnotatedParentAsTests() {
    assertTrue(this.testee.isATestClass(this.classRepostory.fetchClass(
        InheritsFromAnnotatedBase.class).value()));
  }

}
