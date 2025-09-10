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

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classpath.ClassPath;
import org.pitest.mutationtest.config.PluginServices;

public abstract class BasePitMojoTest extends AbstractMojoTestCase {

  @Mock
  protected MavenProject        project;

  @Mock
  protected MavenSession        session;

  @Mock
  protected Settings            settings;

  @Mock
  protected RunPitStrategy      executionStrategy;

  protected List<String>        classPath;

  @Mock
  protected Predicate<Artifact> filter;

  @Mock
  protected PluginServices      plugins;

  Properties properties = new Properties();

  Map<String, Artifact> pluginArtifacts = new HashMap<>();

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    MockitoAnnotations.openMocks(this);
    this.classPath = ClassPath.getClassPathElementsAsFiles().stream()
            .map(File::getAbsolutePath)
            .collect(Collectors.toList());
    when(this.project.getProperties()).thenReturn(properties);
    when(this.project.getTestClasspathElements()).thenReturn(this.classPath);
    when(this.project.getPackaging()).thenReturn("jar");
    
    final Build build = new Build();
    build.setOutputDirectory("");
    
    when(this.project.getBuild()).thenReturn(build);
    
    when(this.plugins.findToolClasspathPlugins()).thenReturn(
        Collections.emptyList());
    when(this.plugins.findClientClasspathPlugins()).thenReturn(
        Collections.emptyList());
  }

  protected String createPomWithConfiguration(final String config) {
    final String pom = "<project>\n" + //
        "  <build>\n" + //
        "    <plugins>\n" + //
        "      <plugin>\n" + //
        "        <groupId>org.pitest</groupId>\n" + //
        "        <artifactId>pitest-maven</artifactId>\n" + //
        "        <configuration>\n" + config + //
        "        </configuration>\n" + //
        "      </plugin>\n" + //
        "    </plugins>\n" + //
        "  </build>\n" + //
        "</project>";
    return pom;
  }

  protected PitMojo createPITMojo(final String config) throws Exception {
    final PitMojo pitMojo = new PitMojo(this.executionStrategy, this.filter,
        this.plugins, p -> true, null);
    configurePitMojo(pitMojo, config);
    return pitMojo;
  }

  protected void configurePitMojo(final PitMojo pitMojo, final String config)
      throws Exception {
    final Xpp3Dom xpp3dom = Xpp3DomBuilder.build(new StringReader(config));
    final PlexusConfiguration pluginConfiguration = extractPluginConfiguration(
        "pitest-maven", xpp3dom);

    // default the report dir to something
    setVariableValueToObject(pitMojo, "reportsDirectory", new File("."));

    configureMojo(pitMojo, pluginConfiguration);

    setVariableValueToObject(pitMojo, "pluginArtifactMap", pluginArtifacts);

    setVariableValueToObject(pitMojo, "project", this.project);
    setVariableValueToObject(pitMojo, "session", this.session);
    setVariableValueToObject(pitMojo, "settings", this.settings);

    if (pitMojo.getAdditionalClasspathElements() == null) {
      ArrayList<String> elements = new ArrayList<>();
      setVariableValueToObject(pitMojo, "additionalClasspathElements", elements);
    }

  }

}
