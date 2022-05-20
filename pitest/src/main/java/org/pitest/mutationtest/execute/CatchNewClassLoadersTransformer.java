package org.pitest.mutationtest.execute;

import org.pitest.boot.HotSwapAgent;
import org.pitest.classinfo.ClassName;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pitest mainly inserts mutants by calling Instrumentation.redefineClasses using
 * the current context classloader. This works in most cases, but if a test
 * runner switches the context classloader and loads classes into it, the
 * mutant will not be active.
 *
 * The transformer acts as a net to catch these instances... but it all gets a bit messy.
 *
 * The classloaders created by the test may (or may not) delegate to their parent classloader,
 * or may only do so partially. They may also transform classes which is problematic as we overwrite
 * the byte array (modifying by rerunning the mutator might result in a different mutant for
 * the same id if the input has changed).
 *
 * So, sometimes we want to transform the class, sometimes we don't.
 *
 * Not found a way to reliably work out which we need to do, so need to maintain
 * a list. As mutants unexpectedly surviving are far more likely to be reported than
 * ones unexpectedly killed, we list the loaders we should mutate for (currently just
 * quarkus, but others likely exist).
 *
 */
public class CatchNewClassLoadersTransformer implements ClassFileTransformer {

    private static byte[] currentMutant;
    private static String currentClass;

    // The context classloader at the point pitest started.
    // we do not want to transform classes from this as they are already handled
    // by the primary mechanism
    private static ClassLoader ignore;

    // Map of loaders we have transformed the current class in, so we can restore them
    static final Map<ClassLoader, byte[]> ORIGINAL_LOADER_CLASSES = new ConcurrentHashMap<>();

    public static synchronized void setLoader(ClassLoader loader) {
        ignore = loader;
    }

    public static synchronized void setMutant(String className, byte[] mutant) {
        String toRestore = currentClass;
        currentMutant = mutant;

        // prevent transforming again when we restore if the same class is mutated twice
        currentClass = null;

        restoreClasses(toRestore);

        currentClass = className;
    }

    private static void restoreClasses(String toRestore) {
        for (Map.Entry<ClassLoader, byte[]> each : ORIGINAL_LOADER_CLASSES.entrySet()) {
            final Class<?> clazz = checkClassForLoader(each.getKey(), toRestore);
            if (clazz != null) {
                HotSwapAgent.hotSwap(clazz, each.getValue());
            }
        }
        ORIGINAL_LOADER_CLASSES.clear();
    }

    @Override
    public byte[] transform(final ClassLoader loader, final String className,
                            final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain, final byte[] classfileBuffer) {
        if (loader == null || ignore == loader) {
            return null;
        }

        // Only loader identified so far where mutants must be inserted is Quarkus, but
        // others likely exist. At least one loader (gwtmockito) results incorrectly
        // killed mutants if we insert.
        if (!loader.getClass().getName().startsWith("io.quarkus.bootstrap.classloading")) {
            return null;
        }

        if (className.equals(currentClass)) {
            // skip if class already loaded
            if (classBeingRedefined != null) {
                return null;
            }

            // avoid restoring an already mutated class
            // Not clear if this situation is possible, but check left in
            // out of fear.
            if (!Arrays.equals(classfileBuffer, currentMutant)) {
                ORIGINAL_LOADER_CLASSES.put(loader, classfileBuffer);
            }

            return currentMutant;
        }
        return null;
    }

    private static Class<?> checkClassForLoader(ClassLoader loader, String className) {
        try {
            Class<?> clazz = Class.forName(ClassName.fromString(className).asJavaName(), false, loader);
            // loaded by parent
            if (clazz.getClassLoader() != loader) {
                return null;
            }
            return clazz;
        } catch (ClassNotFoundException ex) {
            // *think* this occurs as a result of a loader not delegating
            // Does not occur when we only mutate for the Quarkus loader, but
            // left in place for when/if the list expands
            return null;
        }

    }

}
