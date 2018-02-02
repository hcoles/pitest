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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;
import org.pitest.classpath.ClassPathRoot;
import org.pitest.classpath.DirectoryClassPathRoot;
import org.pitest.functional.prelude.Prelude;

public class PathNamePredicateTest {

  private PathNamePredicate testee;

  @Test
  public void shouldMatchRootsWithMatchingNames() {
    final File testFile = new File("/foo/bar");
    final ClassPathRoot root = new DirectoryClassPathRoot(testFile);
    this.testee = new PathNamePredicate(Prelude.isEqualTo(testFile
        .getAbsolutePath()));
    assertThat(this.testee.test(root), is(true));
  }

  @Test
  public void shouldNotMatchRootsWithNonMatchingNames() {
    final ClassPathRoot root = new DirectoryClassPathRoot(new File("/foo/bar/"));
    this.testee = new PathNamePredicate(Prelude.isEqualTo("phoee"));
    assertFalse(this.testee.test(root));
  }

}
