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
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.pitest.boot.CodeCoverageStore;
import org.pitest.boot.HotSwapAgent;
import org.pitest.boot.InvokeReceiver;
import org.pitest.functional.Option;
import org.pitest.internal.ClassByteArraySource;
import org.pitest.internal.ClassPathByteArraySource;
import org.pitest.util.FileUtil;
import org.pitest.util.JavaAgent;
import org.pitest.util.Unchecked;

public class JarCreatingJarFinder implements JavaAgent {

  protected static final String CAN_REDEFINE_CLASSES  = "Can-Redefine-Classes";
  protected static final String PREMAIN_CLASS         = "Premain-Class";

  protected static final String CAN_SET_NATIVE_METHOD = "Can-Set-Native-Method-Prefix";
  protected static final String BOOT_CLASSPATH        = "Boot-Class-Path";

  private final static String   AGENT_CLASS_NAME      = HotSwapAgent.class
                                                          .getName();

  private Option<String>        location              = Option.none();

  private final ClassByteArraySource       classByteSource;

  public JarCreatingJarFinder(final ClassByteArraySource classByteSource) {
    this.classByteSource = classByteSource;
  }

  public JarCreatingJarFinder() {
    this(new ClassPathByteArraySource());
  }

  public Option<String> getJarLocation() {
    if (this.location.hasNone()) {
      this.location = createJar();
    }
    return this.location;
  }

  private Option<String> createJar() {
    try {

      final String randomName = FileUtil.randomFilename() + ".jar";
      final FileOutputStream fos = new FileOutputStream(randomName);
      createJarFromClassPathResources(fos, randomName);
      return Option.some(randomName);

    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

  private void createJarFromClassPathResources(final FileOutputStream fos,
      final String location) throws IOException {
    final Manifest m = new Manifest();

    m.clear();
    final Attributes global = m.getMainAttributes();
    if (global.getValue(Attributes.Name.MANIFEST_VERSION) == null) {
      global.put(Attributes.Name.MANIFEST_VERSION, "1.0");
    }
    final File mylocation = new File(location);
    global.putValue(BOOT_CLASSPATH, getBoothClassPath(mylocation));
    global.putValue(PREMAIN_CLASS, AGENT_CLASS_NAME);
    global.putValue(CAN_REDEFINE_CLASSES, "true");
    global.putValue(CAN_SET_NATIVE_METHOD, "true");

    final JarOutputStream jos = new JarOutputStream(fos, m);
    addClass(HotSwapAgent.class, jos);
    addClass(CodeCoverageStore.class, jos);
    addClass(InvokeReceiver.class, jos);
    jos.close();
  }

  private String getBoothClassPath(final File mylocation) {

    final String path = mylocation.getAbsolutePath().replace('\\', '/');

    // final List<String> agents = getEmmaJarsIfLoaded();
    //
    // for (final String agentJar : agents) {
    // path = path + File.pathSeparator + agentJar;
    // }

    return path;

  }

  private List<String> getEmmaJarsIfLoaded() {
    try {
      final Class<?> cls = Class.forName("com.vladium.emma.rt.RT");
      final ProtectionDomain pDomain = cls.getProtectionDomain();
      final CodeSource cSource = pDomain.getCodeSource();
      final URL loc = cSource.getLocation();
      return Collections.singletonList(loc.getFile());
    } catch (final ClassNotFoundException e) {
      return Collections.emptyList();
    }
  }

  private void addClass(final Class<?> clazz, final JarOutputStream jos)
      throws IOException {
    final String className = clazz.getName();
    final ZipEntry ze = new ZipEntry(className.replace(".", "/") + ".class");
    jos.putNextEntry(ze);
    jos.write(classBytes(className));
    jos.closeEntry();
  }

  private byte[] classBytes(final String className) throws IOException {
    return this.classByteSource.apply(className).value();
  }

  public void close() {
    if (this.location.hasSome()) {
      final File f = new File(this.location.value());
      f.delete();
    }
  }

}
