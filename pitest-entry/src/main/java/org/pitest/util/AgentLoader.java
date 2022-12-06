package org.pitest.util;

import java.io.File;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.io.PrintWriter;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.net.URL;
import java.lang.reflect.InvocationTargetException;

public class AgentLoader {
    private static final String TOOLS_JAR_NAME = "tools.jar";
    private static final String CLASSES_JAR_NAME = "classes.jar";
    private static final String LIB = "lib";

    public static File findToolsJar() {
        // Copied from ekstazi:
        // https://github.com/gliga/ekstazi/blob/6567da0534c20eeee802d2dfb8d216cbcbf6883c/org.ekstazi.core/src/main/java/org/ekstazi/agent/AgentLoader.java#L209
        String javaHome = System.getProperty("java.home");
        File javaHomeFile = new File(javaHome);
        File tjf = new File(javaHomeFile, LIB + File.separator + TOOLS_JAR_NAME);

        if (!tjf.exists()) {
            tjf = new File(System.getenv("java_home"), LIB + File.separator + TOOLS_JAR_NAME);
        }

        if (!tjf.exists() && javaHomeFile.getAbsolutePath().endsWith(File.separator + "jre")) {
            javaHomeFile = javaHomeFile.getParentFile();
            tjf = new File(javaHomeFile, LIB + File.separator + TOOLS_JAR_NAME);
        }
        return tjf;
    }


    public static StringWriter loadAndRunJdeps(List<String> args) {
        StringWriter output = new StringWriter();
        try {
            File toolsJarFile = findToolsJar();
            if (!toolsJarFile.exists()) {
                // Java 9+, load jdeps through java.util.spi.ToolProvider
                Class<?> toolProvider = ClassLoader.getSystemClassLoader().loadClass("java.util.spi.ToolProvider");
                Object jdeps = toolProvider.getMethod("findFirst", String.class).invoke(null, "jdeps");
                jdeps = Optional.class.getMethod("get").invoke(jdeps);
                toolProvider.getMethod("run", PrintWriter.class, PrintWriter.class, String[].class)
                        .invoke(jdeps, new PrintWriter(output), new PrintWriter(output), args.toArray(new String[0]));
            } else {
                // Java 8, load tools.jar
                URLClassLoader loader = new URLClassLoader(new URL[] { toolsJarFile.toURI().toURL() },
                        ClassLoader.getSystemClassLoader());
                Class<?> jdepsMain = loader.loadClass("com.sun.tools.jdeps.Main");
                jdepsMain.getMethod("run", String[].class, PrintWriter.class)
                        .invoke(null, args.toArray(new String[0]), new PrintWriter(output));
            }
        } catch (MalformedURLException malformedURLException) {
            malformedURLException.printStackTrace();
        } catch (ClassNotFoundException classNotFoundException) {
            classNotFoundException.printStackTrace();
        } catch (InvocationTargetException invocationTargetException) {
            invocationTargetException.printStackTrace();
        } catch (IllegalAccessException illegalAccessException) {
            illegalAccessException.printStackTrace();
        } catch (NoSuchMethodException noSuchMethodException) {
            noSuchMethodException.printStackTrace();
        }
        return output;
    }
    
}
