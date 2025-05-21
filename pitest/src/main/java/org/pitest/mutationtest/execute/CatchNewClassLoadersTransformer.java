package org.pitest.mutationtest.execute;

import org.pitest.boot.HotSwapAgent;
import org.pitest.classinfo.ClassName;
import org.pitest.util.Log;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
 * This transformer identifies loaders that try to load the target class, and stores
 * a weak reference to them to ensure that all copies of the class are mutated.
 *
 */
public class CatchNewClassLoadersTransformer implements ClassFileTransformer {

    private static final Logger LOG = Log.getLogger();

    private static String targetClass;
    private static byte[] currentMutant;

    //
    // Storage was introduced to support Quarkus, however they changed their classloading strategy in
    // 3.22.2 so it is no longer necessary. As storage runs the risk of performance degradation if too
    // many classloaders are encountered removing this shouldn be investigated once older versions of
    // quarkus are out of circulation.
    //
    // What we want is a ConcurrentWeakHasSet, since that doesn't exist without writing one ourselves
    // we'll abuse a WeakHashMap and live with the synchronization.
    static final Map<ClassLoader, Object> CLASS_LOADERS = Collections.synchronizedMap(new WeakHashMap<>());

    public static synchronized void setMutant(String className, byte[] mutant) {
        targetClass = className;
        currentMutant = mutant;

        logClassloaders();

        for (ClassLoader each : CLASS_LOADERS.keySet()) {
            final Class<?> clazz = checkClassForLoader(each, className);
            if (clazz != null) {
                HotSwapAgent.hotSwap(clazz, mutant);
            }
        }
    }

    @Override
    public byte[] transform(final ClassLoader loader, final String className,
                            final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain, final byte[] classfileBuffer) {

        if (className.equals(targetClass) && shouldTransform(loader)) {
            if (shouldStore(loader)) {
                CLASS_LOADERS.put(loader, null);
            }
            // we might be mid-mutation so return the mutated bytes
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

    private boolean shouldStore(ClassLoader loader) {
        // quarkus generates large numbers of ParentLastURLClassLoaders. They need to be
        // transformed when first encountered, but do not seem to be used to load the mutated
        // class in subsequent tests. Re-transforming them would introduce a significant performance hit.
        return !loader.getClass().getName().equals("io.quarkus.test.junit.classloading.ParentLastURLClassLoader");
    }

    private boolean shouldTransform(ClassLoader loader) {
        // Only gwtmockito has been identified so far as a loader not to transform
        // but there will be others
        return !loader.getClass().getName().startsWith("com.google.gwtmockito.");
    }

    private static void logClassloaders() {
        int count = CLASS_LOADERS.size();
        if (count > 30) {
            LOG.warning("Accumulated " + CLASS_LOADERS.size() + " classloaders, this will likely degrade performance. Please report this as an issue.");
            LOG.warning("Accumulated classloaders = " + CLASS_LOADERS.keySet().stream().map(Object::toString).collect(Collectors.joining("\n")));
        } else if (count > 1) {
            LOG.fine("Accumulated " + CLASS_LOADERS.size() + " classloaders");
        }

    }

}
