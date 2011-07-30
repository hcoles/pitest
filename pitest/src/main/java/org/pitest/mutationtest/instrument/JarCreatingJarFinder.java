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
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.pitest.boot.CodeCoverageStore;
import org.pitest.boot.HotSwapAgent;
import org.pitest.boot.InvokeReceiver;
import org.pitest.functional.Option;
import org.pitest.internal.ClassPath;
import org.pitest.util.FileUtil;
import org.pitest.util.JavaAgent;
import org.pitest.util.Unchecked;

public class JarCreatingJarFinder implements JavaAgent {

  public static final String  CAN_REDEFINE_CLASSES    = "Can-Redefine-Classes";
  public static final String  PREMAIN_CLASS           = "Premain-Class";
  public static final String  CAN_RETRANSFORM_CLASSES = " Can-Retransform-Classes";
  public static final String  CAN_SET_NATIVE_METHOD   = "Can-Set-Native-Method-Prefix";
  public static final String  BOOT_CLASSPATH          = "Boot-Class-Path";

  private final static String AGENT_CLASS_NAME        = HotSwapAgent.class
                                                          .getName();

  private Option<String>      location                = Option.none();

  public Option<String> getJarLocation() {
    if (this.location.hasNone()) {
      this.location = createJar();
    }
    return this.location;
  }

  private static Option<String> createJar() {
    try {

      final String location = FileUtil.randomFilename() + ".jar";
      final FileOutputStream fos = new FileOutputStream(location);
      createJarFromClassPathResources(fos, location);
      return Option.some(location);

    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

  private static void createJarFromClassPathResources(
      final FileOutputStream fos, final String location) throws IOException {
    final ClassPath cp = new ClassPath();
    final Manifest m = new Manifest();

    m.clear();
    final Attributes global = m.getMainAttributes();
    if (global.getValue(Attributes.Name.MANIFEST_VERSION) == null) {
      global.put(Attributes.Name.MANIFEST_VERSION, "1.0");
    }
    final File l = new File(location);
    global.putValue(BOOT_CLASSPATH, l.getAbsolutePath().replace('\\', '/'));
    global.putValue(PREMAIN_CLASS, AGENT_CLASS_NAME);
    global.putValue(CAN_REDEFINE_CLASSES, "true");
    global.putValue(CAN_SET_NATIVE_METHOD, "true");

    final JarOutputStream jos = new JarOutputStream(fos, m);
    addClass(HotSwapAgent.class, cp, jos);
    addClass(CodeCoverageStore.class, cp, jos);
    addClass(InvokeReceiver.class, cp, jos);
    jos.close();
  }

  private static void addClass(final Class<?> clazz, final ClassPath cp,
      final JarOutputStream jos) throws IOException {
    final String className = clazz.getName();
    final ZipEntry ze = new ZipEntry(className.replace(".", "/") + ".class");
    jos.putNextEntry(ze);
    jos.write(classBytes(className, cp));
    jos.closeEntry();
  }

  private static byte[] classBytes(final String className, final ClassPath cp)
      throws IOException {
    return cp.getClassData(className);
  }

  public void close() {
    if (this.location.hasSome()) {
      final File f = new File(this.location.value());
      f.delete();
    }
  }

}
