package org.pitest.mutationtest.environment;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.pitest.bytecode.ASMVersion;
import org.pitest.bytecode.FrameOptions;
import org.pitest.classinfo.ComputeClassWriter;
import org.pitest.classpath.ClassloaderByteArraySource;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Workaround for an apparent ASM bug - attributes do not come out in
 * the same order when records are transformed, resulting in a changed
 * signature. We therefore transform records on first load to
 * ensure the order remains the same.
 */
public class NormaliseRecordsPlugin implements TransformationPlugin {
    @Override
    public String description() {
        return "Normalise record attributes";
    }

    @Override
    public ClassFileTransformer makeMutationTransformer() {
        return new NormaliseRecordsTransformer();
    }
}

class NormaliseRecordsTransformer implements ClassFileTransformer {

    private final Map<String, String> computeCache = new ConcurrentHashMap<>();

    @Override
    public byte[] transform(final ClassLoader loader, final String className,
                            final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain, final byte[] classfileBuffer) {

        if (shouldInclude(className)) {
            final ClassReader reader = new ClassReader(classfileBuffer);
            final ClassWriter writer = new ComputeClassWriter(
                    new ClassloaderByteArraySource(loader), this.computeCache,
                    FrameOptions.pickFlags(classfileBuffer));

            var normaliser = new NormaliseVisitor(writer);
            reader.accept(normaliser, ClassReader.EXPAND_FRAMES);

            if (normaliser.isRecord) {
                return writer.toByteArray();
            }
            return null;

        } else {
            return null;
        }
    }

    private boolean shouldInclude(final String className) {
        // cheaply exclude classes that we won't be mutating
        return !className.startsWith("java/")
                && !className.startsWith("javax/")
                && !className.startsWith("org/junit");
    }
}


/**
 * Although this appears to be a no-op, in fact it ensures that
 * attributes are ordered in the same way as they would be after a transformation
 * by a mutation operator
 */
class NormaliseVisitor extends ClassVisitor {

    boolean isRecord = false;

    NormaliseVisitor(final ClassVisitor arg0) {
        super(ASMVersion.ASM_VERSION, arg0);
    }

    @Override
    public void visit(final int version, final int access, final String name,
            final String signature, final String superName, final String[] interfaces) {
        isRecord = "java/lang/Record".equals(superName);
        super.visit(version, access, name, signature, superName, interfaces);
    }


    @Override
    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {
        // skip processing if we're not going to use the output
        if (isRecord) {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }
        return null;
    }

}
