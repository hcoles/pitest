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

public class DefaultDependencyPathPredicateTest {

  private DefaultDependencyPathPredicate testee;

  @Before
  public void seyUp() {
    this.testee = new DefaultDependencyPathPredicate();
  }

  @Test
  public void shouldTreatJarFilesAsDependencies() {
    final ClassPathRoot archiveRoot = new ArchiveClassPathRoot(new File(
        "foo.jar"));
    assertTrue(this.testee.apply(archiveRoot));
  }

  @Test
  public void shouldNotTreatDirectoriesAsDependencies() {
    final ClassPathRoot archiveRoot = new DirectoryClassPathRoot(new File(
        "foo/bar/"));
    assertFalse(this.testee.apply(archiveRoot));
  }

}
