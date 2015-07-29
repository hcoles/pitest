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
package org.pitest.maven;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.maven.artifact.Artifact;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.mutationtest.config.PluginServices;

@Category(SystemTest.class)
public class DependencyFilterIT {

  private DependencyFilter testee;

  @Mock
  private Artifact         artifact;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new DependencyFilter(PluginServices.makeForContextLoader());
  }

  @Test
  public void shouldAllowPitestCore() {
    when(this.artifact.getGroupId()).thenReturn("org.pitest");
    when(this.artifact.getArtifactId()).thenReturn("pitest");
    assertTrue(this.testee.apply(this.artifact));
  }

  @Test
  public void shouldNotAllowHtmlReport() {
    when(this.artifact.getGroupId()).thenReturn("org.pitest");
    when(this.artifact.getArtifactId()).thenReturn("pitest-html-report");
    assertFalse(this.testee.apply(this.artifact));
  }

}
