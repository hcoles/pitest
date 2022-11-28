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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.classpath.CodeSource;
import org.pitest.help.PitHelpError;
import org.pitest.util.IsolationUtils;
import org.pitest.util.ResourceFolderByteArraySource;

public class DefaultBuildVerifierTest {

  private DefaultBuildVerifier testee;

  @Mock
  private CodeSource           code;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    this.testee = new DefaultBuildVerifier(code);
  }

  private static class AClass {

  }

  private interface AnInterface {

  }

  @Test
  public void shouldNotThrowErrorForClassCompiledWithDebugInfo() {
    setupClassPath(AClass.class);
    this.testee.verify();
    // pass
  }

  @Test(expected = PitHelpError.class)
  public void shouldThrowErrorForClassCompiledWithoutSourceFileDebugInfo() {
    setupClassPath(new ResourceFolderByteArraySource(), "FooNoSource");
    this.testee.verify();
  }

  @Test
  public void shouldNotThrowErrorForSyntheticClassCompiledWithoutSourceFileDebugInfo() {
    setupClassPath(new ResourceFolderByteArraySource(), "SyntheticNoSourceDebug");
    try {
      this.testee.verify();
    } catch (final PitHelpError ex) {
      fail();
    }
  }

  @Test(expected = PitHelpError.class)
  public void shouldThrowErrorForClassCompiledWithoutLineNumberDebugInfo() {
    setupClassPath(new ResourceFolderByteArraySource(), "FooNoLines");
    this.testee.verify();
  }

  @Test
  public void shouldNotThrowAnErrorWhenNoClassesFound() {
    when(this.code.codeTrees()).thenReturn(Stream.empty());
    try {
      this.testee.verify();
    } catch (final PitHelpError e) {
      fail();
    }
  }

  @Test
  public void shouldNotThrowAnErrorWhenOnlyInterfacesPresent() {
    setupClassPath(AnInterface.class);
    try {
      this.testee.verify();
    } catch (final PitHelpError e) {
      fail();
    }
  }

  @Test
  public void doesNotErrorWhenNoClassesProvided() {
    when(this.code.codeTrees()).thenReturn(Stream.empty());
    assertThatCode(() -> this.testee.verify()).doesNotThrowAnyException();
  }

  private void setupClassPath(final Class<?> clazz) {
    this.setupClassPath(
        new ClassloaderByteArraySource(IsolationUtils.getContextClassLoader()),
        clazz.getName());
  }

  private void setupClassPath(final ClassByteArraySource source,
      final String clazz) {
    ClassTree ct = ClassTree.fromBytes(source.getBytes(clazz).get());
    // doesn't seem to be a way to pass mockito a Supplier so specify each call
    when(this.code.codeTrees()).thenReturn(Stream.of(ct), Stream.of(ct), Stream.of(ct), Stream.of(ct));
  }

}
