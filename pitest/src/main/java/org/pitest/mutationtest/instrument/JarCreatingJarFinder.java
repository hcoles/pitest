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
package org.pitest.mutationtest.instrument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.pitest.functional.Option;
import org.pitest.internal.ClassPath;
import org.pitest.util.FileUtil;
import org.pitest.util.JavaAgent;
import org.pitest.util.Unchecked;

public class JarCreatingJarFinder implements JavaAgent {

  private final static String AGENT_CLASS_NAME = HotSwapAgent.class.getName();

  private Option<String>      location         = Option.none();

  public Option<String> getJarLocation() {
    if (this.location.hasNone()) {
      this.location = createJar();
    }
    return this.location;
  }

  private static InputStream manifest(final ClassPath cp) throws IOException {
    return cp.findResource("Agent-Manifest").openStream();
  }

  private static Option<String> createJar() {
    try {

      final String location = FileUtil.randomFilename() + ".jar";
      final FileOutputStream fos = new FileOutputStream(location);
      createJarFromClassPathResources(fos);
      return Option.some(location);

    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

  private static void createJarFromClassPathResources(final FileOutputStream fos)
      throws IOException {
    final ClassPath cp = new ClassPath();
    final Manifest m = new Manifest(manifest(cp));
    final JarOutputStream jos = new JarOutputStream(fos, m);
    final ZipEntry ze = new ZipEntry(AGENT_CLASS_NAME.replace(".", "/"));
    jos.putNextEntry(ze);
    jos.write(classBytes(cp));
    jos.closeEntry();
    jos.close();
  }

  private static byte[] classBytes(final ClassPath cp) throws IOException {
    return cp.getClassData(AGENT_CLASS_NAME);
  }

  public void close() {
    if (this.location.hasSome()) {
      final File f = new File(this.location.value());
      f.delete();
    }
  }

}
