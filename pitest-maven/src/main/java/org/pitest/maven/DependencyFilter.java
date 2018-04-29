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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.maven.artifact.Artifact;
import java.util.function.Function;
import org.pitest.functional.FCollection;
import java.util.function.Predicate;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.plugin.ClientClasspathPlugin;
import org.pitest.util.Log;
import org.pitest.util.PitError;
import org.pitest.util.StringUtil;

public class DependencyFilter implements Predicate<Artifact> {

  private final Set<GroupIdPair> groups = new HashSet<>();

  public DependencyFilter(PluginServices plugins) {
    final Iterable<? extends ClientClasspathPlugin> runtimePlugins = plugins
        .findClientClasspathPlugins();
    FCollection.mapTo(runtimePlugins, artifactToPair(), this.groups);
    findVendorIdForGroups();
  }

  private static Function<ClientClasspathPlugin, GroupIdPair> artifactToPair() {
    return new Function<ClientClasspathPlugin, GroupIdPair>() {

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

  /**
   * Changes the (Implementation-Vendor,Implementation-Title) pairs
   * by the corresponding (Implementation-Vendor-Id, Implementation-Title) pair
   * when the Implementation-Vendor-Id is available.
   * Targets the fact that, by default, project.groupId is assigned to Implementation-Vendor-Id
   * and project.organization.name is assigned to Implementation-Vendor on the META-INF/MANIFEST.MF file.
   */
  private void findVendorIdForGroups() {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    try {
      //Checks every META-INF/MANIFEST.MF file found in the classpath
      Enumeration<URL> urls = loader.getResources("META-INF/MANIFEST.MF");
      while (urls.hasMoreElements()) {
        URL url = urls.nextElement();

        Manifest manifest = new Manifest(url.openStream());
        Attributes attributes = manifest.getMainAttributes();
        String vendor = attributes.getValue("Implementation-Vendor");
        String vendorId = attributes.getValue("Implementation-Vendor-Id");
        String id = attributes.getValue("Implementation-Title");

        if (StringUtil.isNullOrEmpty(vendor) || StringUtil.isNullOrEmpty(vendorId) || StringUtil.isNullOrEmpty(id)) {
          continue;
        }

        GroupIdPair query = new GroupIdPair(vendor, id);
        if (groups.contains(query)) {
          groups.remove(query);
          groups.add(new GroupIdPair(vendorId, id));
        }
      }
    } catch (IOException exc) {
      Log.getLogger().fine("An exception was thrown while looking for manifest files. Message: " + exc.getMessage());
    }
  }

  @Override
  public boolean test(final Artifact a) {
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
