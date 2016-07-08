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
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.functional.Option;
import org.pitest.help.PitHelpError;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.IsolationUtils;

import com.example.testng.FullyCoveredByTestNGTesteeTest;

public class ConfigurationFactoryTest {

  private ConfigurationFactory testee;

  @Mock
  private TestGroupConfig      groupConfig;

  @Mock
  private ClassByteArraySource source;

  private ClassByteArraySource realSource;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new ConfigurationFactory(this.groupConfig, this.source, 
        Collections.<String>emptyList());
    this.realSource = new ClassloaderByteArraySource(
        IsolationUtils.getContextClassLoader());
    when(this.source.getBytes("org.junit.Test")).thenReturn(
        Option.<byte[]> none());
    when(this.source.getBytes("org.testng.TestNG")).thenReturn(
        Option.<byte[]> none());
  }

  @Test
  public void shouldCreateAConfigurationThatFindsTestNGTestsWhenTestNGOnClassPath() {
    putTestNGOnClasspath();
    assertFalse(this.testee.createConfiguration().testUnitFinder()
        .findTestUnits(FullyCoveredByTestNGTesteeTest.class).isEmpty());
  }

  @Test
  public void shouldNotCreateAConfigurationThatFindsTestNGTestsWhenTestNGNotOnClassPath() {
    putJUnitOnClasspath();
    assertTrue(this.testee.createConfiguration().testUnitFinder()
        .findTestUnits(FullyCoveredByTestNGTesteeTest.class).isEmpty());
  }

  @Test
  public void shouldCreateAConfigurationThatFindsJUnitTestsWhenJUnitOnClassPath() {
    putJUnitOnClasspath();
    assertFalse(this.testee.createConfiguration().testUnitFinder()
        .findTestUnits(ConfigurationFactoryTest.class).isEmpty());
  }

  @Test
  public void shouldNotCreateAConfigurationThatFindsJUnitTestsWhenJUnitNotOnClassPath() {
    putTestNGOnClasspath();
    assertTrue(this.testee.createConfiguration().testUnitFinder()
        .findTestUnits(ConfigurationFactoryTest.class).isEmpty());
  }

  @Test
  public void shouldCreateAConfigurationThatFindsBothTestNGAndJUnitTestsWhenBothAreOnClasspath() {
    putTestNGOnClasspath();
    putJUnitOnClasspath();
    assertFalse(this.testee.createConfiguration().testUnitFinder()
        .findTestUnits(ConfigurationFactoryTest.class).isEmpty());
    assertFalse(this.testee.createConfiguration().testUnitFinder()
        .findTestUnits(FullyCoveredByTestNGTesteeTest.class).isEmpty());
  }

  @Test(expected = PitHelpError.class)
  public void shouldThrowAnErrorIfNeitherTestNGOrJUnitOnClassPath() {
    this.testee.createConfiguration();
  }

  private void putTestNGOnClasspath() {
    when(this.source.getBytes("org.testng.TestNG")).thenReturn(
        this.realSource.getBytes("org.testng.TestNG"));
  }

  private void putJUnitOnClasspath() {
    when(this.source.getBytes("org.junit.Test")).thenReturn(
        this.realSource.getBytes("org.junit.Test"));
  }

}
