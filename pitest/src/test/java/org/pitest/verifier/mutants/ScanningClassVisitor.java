package org.pitest.verifier.mutants;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.pitest.bytecode.ASMVersion;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.AnnotationInfo;
import org.pitest.mutationtest.engine.gregor.BasicContext;
import org.pitest.mutationtest.engine.gregor.ClassInfo;
import org.pitest.mutationtest.engine.gregor.FieldInfo;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.mutationtest.engine.gregor.NoMethodContext;

import java.util.List;

public class ScanningClassVisitor extends ClassVisitor {

    private final List<MethodMutatorFactory> mmfs;

    private ClassInfo classInfo;

    protected ScanningClassVisitor(ClassVisitor classVisitor, List<MethodMutatorFactory> mmfs) {
        super(ASMVersion.asmVersion(), classVisitor);
        this.mmfs = mmfs;
    }

    @Override
    public void visit(final int version, final int access, final String name,
                      final String signature, final String superName, final String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.classInfo = new ClassInfo(access, name, superName);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String methodName,
                                     final String methodDescriptor, final String signature,
                                     final String[] exceptions) {

        MutationContext context = fakeContext(classInfo);

        MethodInfo methodInfo = new MethodInfo()
                .withOwner(classInfo).withAccess(access)
                .withMethodName(methodName).withMethodDescriptor(methodDescriptor);

        MethodVisitor next = super.visitMethod(access, methodName, methodDescriptor, signature, exceptions);
        for (final MethodMutatorFactory each : this.mmfs) {
            MethodVisitor mv = each.create(context, methodInfo, next);
            if (mv != null) {
                next = mv;
            }
        }

        return next;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        AnnotationVisitor next = super.visitAnnotation(descriptor, visible);
        AnnotationInfo annotationInfo = new AnnotationInfo(descriptor, visible);

        BasicContext context = fakeMethodContext();

        for (final MethodMutatorFactory each : this.mmfs) {
            if (each.skipAnnotation(context, annotationInfo)) {
                return null;
            }
        }

        for (final MethodMutatorFactory each : this.mmfs) {
            AnnotationVisitor fv = each.createForAnnotation(context, annotationInfo, next);
            if (fv != null) {
                next = fv;
            }
        }
        return next;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        FieldVisitor next = super.visitField(access, name, descriptor, signature, value);
        FieldInfo fieldInfo = new FieldInfo(access, name, descriptor, signature, value);

        for (final MethodMutatorFactory each : this.mmfs) {
            FieldVisitor fv = each.createForField(fakeMethodContext(), fieldInfo, next);
            if (fv != null) {
                next = fv;
            }
        }

        return next;
    }

    private BasicContext fakeMethodContext() {
        return new BasicContext() {
            @Override
            public ClassInfo getClassInfo() {
                return classInfo;
            }

            @Override
            public boolean shouldMutate(MutationIdentifier id) {
                return false;
            }

            @Override
            public void registerMutation(MutationIdentifier id, String description) {

            }
        };
    }


    private MutationContext fakeContext(ClassInfo classInfo) {
        return new MutationContext() {
            @Override
            public void registerCurrentLine(int line) {

            }

            @Override
            public MutationIdentifier registerMutation(MethodMutatorFactory factory, String description) {
                return new MutationIdentifier(Location.location(ClassName.fromString("fake"), "fake", "fake"), 1, "");
            }

            @Override
            public ClassInfo getClassInfo() {
                return classInfo;
            }

            @Override
            public void registerMutation(MutationIdentifier id, String description) {

            }

            @Override
            public boolean shouldMutate(MutationIdentifier newId) {
                return false;
            }

            @Override
            public void registerNewBlock() {

            }

            @Override
            public void registerNewMethodStart() {

            }
        };
    }
}
