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

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.pitest.functional.F;

public class MutatingClassVisitor extends ClassAdapter {

  private final F<MethodInfo, Boolean>    filter;
  private final Context                   context;
  private final Set<MethodMutatorFactory> methodMutators = new HashSet<MethodMutatorFactory>();
  private final PremutationClassInfo      classInfo;

  public MutatingClassVisitor(final ClassVisitor delegateClassVisitor,
      final Context context, final F<MethodInfo, Boolean> filter,
      final PremutationClassInfo classInfo,
      final Collection<MethodMutatorFactory> mutators) {
    super(delegateClassVisitor);
    this.context = context;
    this.filter = filter;
    this.methodMutators.addAll(mutators);
    this.classInfo = classInfo;
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
      final String methodDescriptor, final String signature, final String[] exceptions) {
    this.context.registerMethod(methodName);
    final MethodVisitor methodVisitor = this.cv.visitMethod(access, methodName,
        methodDescriptor, signature, exceptions);

    final MethodInfo info = new MethodInfo()
        .withOwner(this.context.getClassInfo())
        .withAccess(access)
        .withMethodName(methodName)
        .withMethodDescriptor(methodDescriptor);
    
    if (this.filter.apply(info)) {
      return this.visitMethodForMutation(info, methodVisitor);
    } else {
      return methodVisitor;
    }

  }

  private MethodVisitor visitMethodForMutation(final MethodInfo methodInfo,
      final MethodVisitor methodVisitor) {

    MethodVisitor next = methodVisitor;
    for (final MethodMutatorFactory each : this.methodMutators) {
      next = each.create(this.context, methodInfo, next);
    }

    return wrapWithLineTracker(wrapWithLineFilter(next));
  }

  private MethodVisitor wrapWithLineTracker(MethodVisitor wrappedMethodVisitor) {
    return new LineTrackingMethodVisitor(this.context, wrappedMethodVisitor);
  }

  private MethodVisitor wrapWithLineFilter(
      final MethodVisitor wrappedMethodVisitor) {
    return new LineFilterMethodAdapter(this.context, this.classInfo,
        wrappedMethodVisitor);
  }

}
