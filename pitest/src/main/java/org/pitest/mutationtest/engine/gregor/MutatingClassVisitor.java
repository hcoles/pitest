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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.pitest.bytecode.ASMVersion;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.gregor.analysis.InstructionTrackingMethodVisitor;
import org.pitest.mutationtest.engine.gregor.blocks.BlockTrackingMethodDecorator;

class MutatingClassVisitor extends ClassVisitor {

  private final Predicate<MethodInfo>    filter;
  private final ClassContext              context;
  private final Set<MethodMutatorFactory> methodMutators = new HashSet<>();

  MutatingClassVisitor(final ClassVisitor delegateClassVisitor,
      final ClassContext context, final Predicate<MethodInfo> filter,
      final Collection<MethodMutatorFactory> mutators) {
    super(ASMVersion.ASM_VERSION, delegateClassVisitor);
    this.context = context;
    this.filter = filter;
    this.methodMutators.addAll(mutators);
  }

  @Override
  public void visit(final int version, final int access, final String name,
      final String signature, final String superName, final String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces);
    this.context.registerClass(new ClassInfo(version, access, name, signature,
        superName, interfaces));
  }

  @Override
  public void visitSource(final String source, final String debug) {
    super.visitSource(source, debug);
    this.context.registerSourceFile(source);
  }

  @Override
  public MethodVisitor visitMethod(final int access, final String methodName,
      final String methodDescriptor, final String signature,
      final String[] exceptions) {

    final MethodMutationContext methodContext = new MethodMutationContext(
        this.context, Location.location(
            ClassName.fromString(this.context.getClassInfo().getName()),
            MethodName.fromString(methodName), methodDescriptor));

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

  private MethodVisitor visitMethodForMutation(
      MethodMutationContext methodContext, final MethodInfo methodInfo,
      final MethodVisitor methodVisitor) {

    MethodVisitor next = methodVisitor;
    for (final MethodMutatorFactory each : this.methodMutators) {
      next = each.create(methodContext, methodInfo, next);
    }

    return new InstructionTrackingMethodVisitor(wrapWithDecorators(
        methodContext, wrapWithFilters(methodContext, next)), methodContext);
  }

  private static MethodVisitor wrapWithDecorators(MethodMutationContext methodContext,
      final MethodVisitor mv) {
    return wrapWithBlockTracker(methodContext,
        wrapWithLineTracker(methodContext, mv));
  }

  private static MethodVisitor wrapWithBlockTracker(
      MethodMutationContext methodContext, final MethodVisitor mv) {
    return new BlockTrackingMethodDecorator(methodContext, mv);
  }

  private static MethodVisitor wrapWithLineTracker(
      MethodMutationContext methodContext, final MethodVisitor mv) {
    return new LineTrackingMethodVisitor(methodContext, mv);
  }

  private MethodVisitor wrapWithFilters(MethodMutationContext methodContext,
      final MethodVisitor wrappedMethodVisitor) {
    return
        wrapWithStringSwitchFilter(methodContext, wrapWithAssertFilter(methodContext, wrappedMethodVisitor));
  }

  private static MethodVisitor wrapWithStringSwitchFilter(
    MethodMutationContext methodContext,
    final MethodVisitor wrappedMethodVisitor) {
  return new AvoidStringSwitchedMethodAdapter(methodContext, wrappedMethodVisitor);

  }

  private static MethodVisitor wrapWithAssertFilter(
      MethodMutationContext methodContext,
      final MethodVisitor wrappedMethodVisitor) {
    return new AvoidAssertsMethodAdapter(methodContext, wrappedMethodVisitor);
  }

}
