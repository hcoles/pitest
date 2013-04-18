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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.pitest.functional.predicate.Predicate;

public class DependencyFilter implements Predicate<Artifact> {

  private final Set<String> allowedGroups = new HashSet<String>();

  public DependencyFilter(final String... groups) {
    this.allowedGroups.addAll(Arrays.asList(groups));
  }

  public Boolean apply(final Artifact a) {
    // mutation engines must be available on the classpath of
    // the slave processes. This enabled by naming convention.
    return this.allowedGroups.contains(a.getGroupId()) || a.getArtifactId().equals("mutation-engine");
  }

}
