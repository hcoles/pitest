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
package org.pitest.mutationtest.tooling;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.Reader;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.functional.F;
import org.pitest.functional.Option;

public class DirectorySourceLocatorTest {

  private DirectorySourceLocator testee;
  private File                   root;

  @Mock
  F<File, Option<Reader>>        locator;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.root = new File(".");
    this.testee = new DirectorySourceLocator(this.root, this.locator);
    when(this.locator.apply(any(File.class)))
    .thenReturn(Option.<Reader> none());
  }

  @Test
  public void shouldLocateSourceForClassesInDefaultPackage() {
    this.testee.locate(Collections.singletonList("Foo"), "Foo.java");
    final File expected = new File(this.root + File.separator + "Foo.java");
    verify(this.locator).apply(expected);
  }

  @Test
  public void shouldLocateSourceForClassesInNamedPacakges() {
    this.testee
    .locate(Collections.singletonList("com.example.Foo"), "Foo.java");
    final File expected = new File(this.root + File.separator + "com"
        + File.separator + "example" + File.separator + "Foo.java");
    verify(this.locator).apply(expected);
  }
}
