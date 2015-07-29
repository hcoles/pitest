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

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.plugin.ClientClasspathPlugin;
import org.pitest.util.PitError;

public class DependencyFilter implements Predicate<Artifact> {

  private final Set<GroupIdPair> groups = new HashSet<GroupIdPair>();

  public DependencyFilter(PluginServices plugins) {
    final Iterable<? extends ClientClasspathPlugin> runtimePlugins = plugins
        .findClientClasspathPlugins();
    FCollection.mapTo(runtimePlugins, artifactToPair(), this.groups);
  }

  private static F<ClientClasspathPlugin, GroupIdPair> artifactToPair() {
    return new F<ClientClasspathPlugin, GroupIdPair>() {

      @Override
      public GroupIdPair apply(final ClientClasspathPlugin a) {
        final Package p = a.getClass().getPackage();

        final GroupIdPair g = new GroupIdPair(p.getImplementationVendor(),
            p.getImplementationTitle());

        if (g.id == null) {
          reportBadPlugin("title", a);
        }

        if (g.group == null) {
          reportBadPlugin("vendor", a);
        }

        return g;

      }

      private void reportBadPlugin(final String missingProperty,
          final ClientClasspathPlugin a) {
        final Class<?> clss = a.getClass();
        throw new PitError("No implementation " + missingProperty
            + " in manifest of plugin jar for " + clss + " in "
            + clss.getProtectionDomain().getCodeSource().getLocation());
      }

    };
  }

  @Override
  public Boolean apply(final Artifact a) {
    final GroupIdPair p = new GroupIdPair(a.getGroupId(), a.getArtifactId());
    return this.groups.contains(p);
  }

  private static class GroupIdPair {
    private final String group;
    private final String id;

    GroupIdPair(final String group, final String id) {
      this.group = group;
      this.id = id;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = (prime * result)
          + ((this.group == null) ? 0 : this.group.hashCode());
      result = (prime * result) + ((this.id == null) ? 0 : this.id.hashCode());
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final GroupIdPair other = (GroupIdPair) obj;
      if (this.group == null) {
        if (other.group != null) {
          return false;
        }
      } else if (!this.group.equals(other.group)) {
        return false;
      }
      if (this.id == null) {
        if (other.id != null) {
          return false;
        }
      } else if (!this.id.equals(other.id)) {
        return false;
      }
      return true;
    }

  }

}
