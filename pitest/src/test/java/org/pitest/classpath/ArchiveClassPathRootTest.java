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
package org.pitest.classpath;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

public class ArchiveClassPathRootTest {

  private ArchiveClassPathRoot testee;

  @Before
  public void setup() throws Exception {
    // note mytests.jar is taken from
    // http://johanneslink.net/projects/cpsuite.jsp
    // assume GPL licence for this file. We do not link to any code within it
    // however
    this.testee = new ArchiveClassPathRoot(new File("mytests.jar"));
  }

  @Test
  public void classNamesShouldReturnAllClassNamesIArchive() {
    final Collection<String> expected = Arrays.asList(
        "injar.p1.P1NoTest$InnerTest", "injar.p1.P1NoTest", "injar.p1.P1Test",
        "injar.p2.P2Test");
    assertThat(this.testee.classNames()).isEqualTo(expected);
  }

  @Test
  public void getDataShouldReturnNullForUnknownClass() throws Exception {
    assertThat(this.testee.getData("bar")).isNull();
  }

  @Test
  public void getDataShouldReturnInputStreamForAKnownClass() throws Exception {
    assertThat(this.testee.getData("injar.p1.P1Test")).isNotNull();
  }

  @Test
  public void shouldReturnAReadableInputStream() {
    final byte b[] = new byte[100];
    try {
      final InputStream actual = this.testee.getData("injar.p1.P1Test");
      actual.read(b);
    } catch (final IOException ex) {
      assertThat(ex).isNull(); // This will fail if an exception occurs
    }
  }

  @Test
  public void getResourceShouldReturnNullForAnUnknownResource()
      throws Exception {
    assertThat(this.testee.getResource("bar")).isNull();
  }

  @Test
  public void getResourceShouldReturnURLForAKnownResource() throws Exception {
    assertThat(this.testee.getResource("injar/p1/P1Test.class")).isNotNull();
  }

}
