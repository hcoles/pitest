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
package org.pitest.mutationtest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.Option;
import org.pitest.internal.classloader.ArchiveClassPathRoot;
import org.pitest.internal.classloader.ClassPathRoot;
import org.pitest.internal.classloader.DirectoryClassPathRoot;

public class DefaultCodePathPredicateTest {

  private DefaultCodePathPredicate testee;

  @Before
  public void seyUp() {
    this.testee = new DefaultCodePathPredicate(50);
  }

  @Test
  public void shouldNotTreatJarFilesAsCode() {
    ClassPathRoot archiveRoot = new ArchiveClassPathRoot(new File("foo.jar"));
    assertFalse(this.testee.apply(archiveRoot));
  }

  @Test
  public void shouldNotTreatDirectoriesEndingInTestClassesAsCode() {
    ClassPathRoot archiveRoot = new DirectoryClassPathRoot(new File(
        "foo/bar/test-classes"));
    assertFalse(this.testee.apply(archiveRoot));
  }

  @Test
  public void shouldNotTreatRootsWhereMoreThanThresholdOfClassesAreTestsAsCode() {
    ClassPathRoot archiveRoot = mock(ClassPathRoot.class);
    when(archiveRoot.cacheLocation()).thenReturn(Option.some("foo"));
    when(archiveRoot.classNames()).thenReturn(
        Arrays.asList("Code", "TestCode", "CodeTest", "Test", "YetMoreCode"));
    assertFalse(this.testee.apply(archiveRoot));
  }

  @Test
  public void shouldTreatRootsWhereLessThenThresholdOfClassesAreTestsAsCode() {
    ClassPathRoot archiveRoot = mock(ClassPathRoot.class);
    when(archiveRoot.cacheLocation()).thenReturn(Option.some("foo"));
    when(archiveRoot.classNames()).thenReturn(
        Arrays
            .asList("Code", "TestCode", "CodeTest", "MoreCode", "YetMoreCode"));
    assertTrue(this.testee.apply(archiveRoot));
  }

}
