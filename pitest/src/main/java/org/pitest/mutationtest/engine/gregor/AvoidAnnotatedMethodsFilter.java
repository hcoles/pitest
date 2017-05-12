/*
 * Copyright 2017 Henry Coles
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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Disables mutations within methods with matching annotations.
 * 
 * The list is currently hard coded to match any annotations named
 * 
 *   Generated
 *   DoNotMutate
 *   CoverageIgnore
 */
public class AvoidAnnotatedMethodsFilter extends MethodVisitor {

  private static final String   DISABLE_REASON = "ANNOTATED";

  private final MutationContext context;

  public AvoidAnnotatedMethodsFilter(final MutationContext context,
      final MethodVisitor delegateMethodVisitor) {
    super(Opcodes.ASM5, delegateMethodVisitor);
    this.context = context;
  }

  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    if (desc.endsWith("Generated;") 
     || desc.endsWith("DoNotMutate;") 
     || desc.endsWith("CoverageIgnore;") ) {
      this.context.disableMutations(DISABLE_REASON);
    }
    return null;
  }

  @Override
  public void visitEnd() {
    this.context.enableMutatations(DISABLE_REASON);
  }

}
