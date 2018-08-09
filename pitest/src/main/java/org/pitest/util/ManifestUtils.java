/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pitest.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

/**
 * Ugly static methods to create and parse classpath manifests
 */
public class ManifestUtils {

  // Method based on
  // https://github.com/JetBrains/intellij-community/blob/master/java/java-runtime/src/com/intellij/rt/execution/testFrameworks/ForkedByModuleSplitter.java
  // JetBrains copyright notice and licence retained above.
  public static File createClasspathJarFile(String classpath)
      throws IOException {
    final Manifest manifest = new Manifest();
    final Attributes attributes = manifest.getMainAttributes();
    attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");

    String classpathForManifest = "";
    int idx = 0;
    int endIdx = 0;
    while (endIdx >= 0) {
      endIdx = classpath.indexOf(File.pathSeparator, idx);
      String path = endIdx < 0 ? classpath.substring(idx)
          : classpath.substring(idx, endIdx);
      if (classpathForManifest.length() > 0) {
        classpathForManifest += " ";
      }

      classpathForManifest += new File(path).toURI().toURL().toString();
      idx = endIdx + File.pathSeparator.length();
    }
    attributes.put(Attributes.Name.CLASS_PATH, classpathForManifest);

    File jarFile = File.createTempFile("classpath", ".jar");
    try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(jarFile));
        ZipOutputStream jarPlugin = new JarOutputStream(out, manifest);
        )  {
      jarFile.deleteOnExit(); 
    }

    return jarFile;
  }
  
  public static Collection<File> readClasspathManifest(File file) {
    try (FileInputStream fis = new FileInputStream(file);
        JarInputStream jarStream = new JarInputStream(fis);) {
      Manifest mf = jarStream.getManifest();
      Attributes att = mf.getMainAttributes();
      String cp = att.getValue(Attributes.Name.CLASS_PATH);
      String[] parts = cp.split("file:");
      return Arrays.stream(parts)
          .filter(part -> !part.isEmpty())
          .map(part -> new File(part.trim()))
          .collect(Collectors.toList());      
    } catch (IOException ex) {
      throw new RuntimeException("Could not read classpath jar manifest", ex);
    }
  }
}
