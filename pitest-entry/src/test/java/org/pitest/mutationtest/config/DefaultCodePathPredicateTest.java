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
package org.pitest.mutationtest.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.pitest.classpath.ArchiveClassPathRoot;
import org.pitest.classpath.ClassPathRoot;
import org.pitest.classpath.DirectoryClassPathRoot;

public class DefaultCodePathPredicateTest {

  private DefaultCodePathPredicate testee;

  @Before
  public void seyUp() {
    this.testee = new DefaultCodePathPredicate();
  }

  @Test
  public void shouldNotTreatJarFilesAsCode() {
    final ClassPathRoot archiveRoot = new ArchiveClassPathRoot(new File(
        "foo.jar"));
    assertFalse(this.testee.apply(archiveRoot));
  }

  @Test
  public void shouldNotTreatZipFilesAsCode() {
    final ClassPathRoot archiveRoot = new ArchiveClassPathRoot(new File(
        "foo.zip"));
    assertFalse(this.testee.apply(archiveRoot));
  }

  @Test
  public void shouldNotTreatDirectoriesEndingInTestClassesAsCode() {
    final ClassPathRoot archiveRoot = new DirectoryClassPathRoot(new File(
        "foo/bar/test-classes"));
    assertFalse(this.testee.apply(archiveRoot));
  }

  @Test
  public void shouldNotTreatDirectoriesEndingInBinTestAsCode() {
    final ClassPathRoot archiveRoot = new DirectoryClassPathRoot(new File(
        "foo/bar/bin-test"));
    assertFalse(this.testee.apply(archiveRoot));
  }

  @Test
  public void shouldTreatDirectoriesAsCode() {
    final ClassPathRoot archiveRoot = new DirectoryClassPathRoot(new File(
        "foo/bar/"));
    assertTrue(this.testee.apply(archiveRoot));
  }
}
