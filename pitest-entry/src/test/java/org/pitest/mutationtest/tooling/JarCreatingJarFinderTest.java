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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.boot.HotSwapAgent;
import org.pitest.classinfo.ClassByteArraySource;
import java.util.Optional;
import org.pitest.util.PitError;

public class JarCreatingJarFinderTest {

  private JarCreatingJarFinder   testee;

  @Mock
  private ClassByteArraySource   byteSource;

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    when(this.byteSource.getBytes(anyString())).thenReturn(
        Optional.ofNullable(new byte[1]));
    this.testee = new JarCreatingJarFinder(this.byteSource);
  }

  @After
  public void cleanup() {
    if (this.testee != null) {
      this.testee.close();
    }
  }

  @Test
  public void shouldConstructWithoutError() {
    new JarCreatingJarFinder();
    // pass
  }

  @Test
  public void shouldCloseWithoutErrorWhenNoLocationSet() {
    this.testee.close();
    // pass
  }

  @Test
  public void shouldCreateJarFile() {
    final Optional<String> actual = this.testee.getJarLocation();
    assertTrue(actual.isPresent());
  }

  @Test
  public void shouldCreateJarFileInSystemTempDirectory() {
    final String tempDirLocation = System.getProperty("java.io.tmpdir");
    final Optional<String> actual = this.testee.getJarLocation();
    assertTrue(actual.get().startsWith(tempDirLocation));
  }

  @Test
  public void shouldSetPreMainClassAttribute() throws IOException {
    assertGeneratedManifestEntryEquals(JarCreatingJarFinder.PREMAIN_CLASS,
        HotSwapAgent.class.getName());
  }

  @Test
  public void shouldSetCanRedefineClasses() throws IOException {
    assertGeneratedManifestEntryEquals(
        JarCreatingJarFinder.CAN_REDEFINE_CLASSES, "true");
  }

  @Test
  public void shouldSetNativeMethodPrefix() throws IOException {
    assertGeneratedManifestEntryEquals(
        JarCreatingJarFinder.CAN_SET_NATIVE_METHOD, "true");
  }

  @Test
  public void shouldAddPITToTheBootClassPath() throws IOException {
    final String actual = getGeneratedManifestAttribute(JarCreatingJarFinder.BOOT_CLASSPATH);
    assertTrue(!actual.equals(""));
  }

  @Test
  public void shouldFailOnUnreadableRessources() throws IOException {
    this.thrown.expect(PitError.class);

    when(this.byteSource.getBytes(anyString())).thenReturn(
        Optional.<byte[]> empty());

    this.testee.getJarLocation();
  }

  private void assertGeneratedManifestEntryEquals(final String key,
      final String expected) throws IOException, FileNotFoundException {
    final String am = getGeneratedManifestAttribute(key);
    assertEquals(expected, am);
  }

  private String getGeneratedManifestAttribute(final String key)
      throws IOException, FileNotFoundException {
    final Optional<String> actual = this.testee.getJarLocation();
    final File f = new File(actual.get());
    try (JarInputStream jis = new JarInputStream(new FileInputStream(f))) {
      final Manifest m = jis.getManifest();
      final Attributes a = m.getMainAttributes();
      final String am = a.getValue(key);

      return am;
    }
  }
}
