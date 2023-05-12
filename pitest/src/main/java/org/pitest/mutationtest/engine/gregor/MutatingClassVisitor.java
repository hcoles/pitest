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
package org.pitest.mutationtest.engine.gregor;

import java.util.List;
import java.util.function.Predicate;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.pitest.bytecode.ASMVersion;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.gregor.analysis.InstructionTrackingMethodVisitor;
import org.pitest.mutationtest.engine.gregor.blocks.BlockTrackingMethodDecorator;

class MutatingClassVisitor extends ClassVisitor {
  private final Predicate<MethodInfo>    filter;
  private final ClassContext              context;
  private final List<MethodMutatorFactory> mutators;

  private final NoMethodContext nonMethodContext;

  MutatingClassVisitor(final ClassVisitor delegateClassVisitor,
      final ClassContext context, final Predicate<MethodInfo> filter,
      final List<MethodMutatorFactory> mutators) {
    super(ASMVersion.ASM_VERSION, delegateClassVisitor);
    this.context = context;
    this.filter = filter;
    this.mutators = mutators;
    this.nonMethodContext = new NoMethodContext(context);
  }

  @Override
  public void visit(final int version, final int access, final String name,
      final String signature, final String superName, final String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces);
    this.context.registerClass(new ClassInfo(access, name,
        superName));
  }

  @Override
  public void visitSource(final String source, final String debug) {
    super.visitSource(source, debug);
    this.context.registerSourceFile(source);
  }

  @Override
  public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
    AnnotationInfo annotationInfo = new AnnotationInfo(descriptor, visible);
    for (final MethodMutatorFactory each : this.mutators) {
      if (each.skipAnnotation(this.nonMethodContext, annotationInfo)) {
        return null;
      }
    }
    AnnotationVisitor next = super.visitAnnotation(descriptor, visible);
    for (final MethodMutatorFactory each : this.mutators) {
      AnnotationVisitor fv = each.createForAnnotation(this.nonMethodContext, annotationInfo, next);
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
      for (final MethodMutatorFactory each : this.mutators) {
        FieldVisitor fv = each.createForField(this.nonMethodContext, fieldInfo, next);
        if (fv != null) {
          next = fv;
        }
      }
      return next;
  }

  @Override
  public MethodVisitor visitMethod(final int access, final String methodName,
      final String methodDescriptor, final String signature,
      final String[] exceptions) {

    final MethodMutationContext methodContext = new MethodMutationContext(
        this.context, Location.location(
            ClassName.fromString(this.context.getClassInfo().getName()),
            methodName, methodDescriptor));

    final MethodVisitor methodVisitor = this.cv.visitMethod(access, methodName,
        methodDescriptor, signature, exceptions);

    final MethodInfo info = new MethodInfo()
    .withOwner(this.context.getClassInfo()).withAccess(access)
    .withMethodName(methodName).withMethodDescriptor(methodDescriptor);

    if (this.filter.test(info)) {
      return this.visitMethodForMutation(methodContext, info, methodVisitor);
    } else {
      return methodVisitor;
    }

  }

  private static MethodVisitor wrapWithDecorators(
      MethodMutationContext methodContext, final MethodVisitor mv, final MethodInfo methodInfo) {
    return wrapWithBlockTracker(methodContext,
        wrapWithInstructionTracker(methodContext,
            wrapWithLineTracker(methodContext, mv)), methodInfo);
  }

  private static MethodVisitor wrapWithInstructionTracker(
      MethodMutationContext methodContext, final MethodVisitor mv) {
    return new InstructionTrackingMethodVisitor(mv, methodContext);
  }

  private static MethodVisitor wrapWithBlockTracker(
      MethodMutationContext methodContext, final MethodVisitor mv, final MethodInfo methodInfo) {
    return new BlockTrackingMethodDecorator(methodContext, mv, methodInfo.getAccess(), methodInfo.getName(), methodInfo.getMethodDescriptor(), null, null);
  }

  private MethodVisitor visitMethodForMutation(
      MethodMutationContext methodContext, final MethodInfo methodInfo,
      final MethodVisitor methodVisitor) {

    MethodVisitor next = methodVisitor;
    for (final MethodMutatorFactory each : this.mutators) {
      MethodVisitor mv = each.create(methodContext, methodInfo, next);
      if (mv != null) {
        next = mv;
      }
    }

    return wrapWithDecorators(methodContext, next, methodInfo);
  }

  private static MethodVisitor wrapWithLineTracker(
      MethodMutationContext methodContext, final MethodVisitor mv) {
    return new LineTrackingMethodVisitor(methodContext, mv);
  }


}
