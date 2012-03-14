/*
 * Copyright 2012 Henry Coles
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

package org.pitest.mutationtest.verify;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.Repository;
import org.pitest.functional.Option;
import org.pitest.help.PitHelpError;
import org.pitest.internal.ClassByteArraySource;
import org.pitest.internal.ClassPath;
import org.pitest.internal.ClassloaderByteArraySource;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.classloader.ClassPathRoot;
import org.pitest.mutationtest.CoverageDatabase;
import org.pitest.util.Unchecked;

public class DefaultBuildVerifierTest {

  private DefaultBuildVerifier testee;

  @Mock
  private CoverageDatabase     coverageDatabase;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new DefaultBuildVerifier();
  }

  private static interface AnInterface {

  }

  @Test
  public void shouldNotThrowErrorForInterfaceCompiledWithDebugInfo() {
    setupClassPath(AnInterface.class);
    this.testee.verify(this.coverageDatabase);
    // pass
  }

  private static class AClass {

  }

  @Test
  public void shouldNotThrowErrorForClassCompiledWithDebugInfo() {
    setupClassPath(AClass.class);
    this.testee.verify(this.coverageDatabase);
    // pass
  }
  
  @Test(expected = PitHelpError.class)
  public void shouldThrowErrorForClassCompiledWithoutSourceFileDebugInfo() {
    setupClassPath(new ResourceFolderBytesArraySource(), "FooNoSource");
    this.testee.verify(this.coverageDatabase);
  }

  @Test(expected = PitHelpError.class)
  public void shouldThrowErrorForClassCompiledWithoutLineNumberDebugInfo() {
    setupClassPath(new ResourceFolderBytesArraySource(), "FooNoLines");
    this.testee.verify(this.coverageDatabase);
  }

  private void setupClassPath(final Class<?> clazz) {
    this.setupClassPath(
        new ClassloaderByteArraySource(IsolationUtils.getContextClassLoader()),
        clazz.getName());
  }

  private void setupClassPath(final ClassByteArraySource source,
      final String clazz) {
    final Repository repository = new Repository(source);
    final ClassInfo ci = repository.fetchClass(clazz).value();
    when(this.coverageDatabase.getCodeClasses()).thenReturn(
        Collections.singletonList(ci));
  }

  static class ResourceFolderBytesArraySource implements ClassByteArraySource {

    public Option<byte[]> apply(final String classname) {
      final ClassPath cp = new ClassPath(new ResourceFolderClassPathroot());
      try {
        return Option.some(cp.getClassData(classname));
      } catch (final IOException ex) {
        throw Unchecked.translateCheckedException(ex);
      }

    }

  }

  static class ResourceFolderClassPathroot implements ClassPathRoot {

    public URL getResource(final String name) throws MalformedURLException {
      return null;
    }

    public InputStream getData(final String name) throws IOException {
      final String path = "sampleClasses/" + name.replace(".", "/")
          + ".class.bin";
      return IsolationUtils.getContextClassLoader().getResourceAsStream(path);
    }

    public Collection<String> classNames() {
      return null;
    }

    public Option<String> cacheLocation() {
      return null;
    }

  }

}
