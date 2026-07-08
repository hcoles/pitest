package org.pitest.mutationtest.jacoco;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.ASMVersion;
import org.pitest.bytecode.FrameOptions;
import org.pitest.classinfo.ComputeClassWriter;
import org.pitest.classpath.ClassloaderByteArraySource;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DisableJacocoTransformer implements ClassFileTransformer {

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


            reader.accept(new JacocoClassVisitor(writer),
                    ClassReader.EXPAND_FRAMES);
            return writer.toByteArray();
        } else {
            return null;
        }
    }

    private boolean shouldInclude(final String className) {
        return className.equals("org/jacoco/core/instr/Instrumenter");
    }
}


/**
 * Replace the org.jacoco.core.instr.Instrumenter::instrument method
 * with a minimal method that returns the original byte array.
 */
class JacocoClassVisitor extends ClassVisitor {

    JacocoClassVisitor(final ClassVisitor arg0) {
        super(ASMVersion.ASM_VERSION, arg0);
    }


    @Override
    public MethodVisitor visitMethod(final int access, final String name,
                                     final String desc, final String signature, final String[] exceptions) {

        if (name.equals("instrument") && desc.equals("([B)[B")) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
            return null;
        } else {
            return this.cv.visitMethod(access, name, desc, signature, exceptions);
        }

    }

}
