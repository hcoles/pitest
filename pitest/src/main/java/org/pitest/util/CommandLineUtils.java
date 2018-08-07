package org.pitest.util;

import java.io.File;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipOutputStream;

public class CommandLineUtils {

    // Method copied from: https://github.com/JetBrains/intellij-community/blob/master/java/java-runtime/src/com/intellij/rt/execution/testFrameworks/ForkedByModuleSplitter.java
    public static File createClasspathJarFile(Manifest manifest, String classpath) throws IOException {
        final Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");

        String classpathForManifest = "";
        int idx = 0;
        int endIdx = 0;
        while (endIdx >= 0) {
            endIdx = classpath.indexOf(File.pathSeparator, idx);
            String path = endIdx < 0 ? classpath.substring(idx) : classpath.substring(idx, endIdx);
            if (classpathForManifest.length() > 0) {
                classpathForManifest += " ";
            }
            try {
                //noinspection Since15
                classpathForManifest += new File(path).toURI().toURL().toString();
            } catch (NoSuchMethodError e) {
                classpathForManifest += new File(path).toURL().toString();
            }
            idx = endIdx + File.pathSeparator.length();
        }
        attributes.put(Attributes.Name.CLASS_PATH, classpathForManifest);

        File jarFile = File.createTempFile("classpath", ".jar");
        ZipOutputStream jarPlugin = null;
        try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(jarFile));
            jarPlugin = new JarOutputStream(out, manifest);
        } finally {
            if (jarPlugin != null) {
                jarPlugin.close();
            }
        }
        jarFile.deleteOnExit();
        return jarFile;
    }
}
