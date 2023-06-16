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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.plugin.ClientClasspathPlugin;

/**
 * A <code>MethodMutatorFactory</code> is a factory creating method mutating
 * visitors. These visitors will serve two purposes: finding new
 * mutation points (locations in byte code where mutations can be applied) and
 * applying those mutations to the byte code.
 *
 * Name of this class is misleading as it is capable of mutating methods, annotations
 * and field, but it is retained for historic reasons.
 *
 * <p>
 * A <code>MethodMutatorFactory</code> will have a globally unique id and must
 * provide a human readable name via the <code>getName()</code> method. This
 * name will be used in the reports created to document and describe the
 * mutation(s) applied.
 * </p>
 *
 * @author Henry Coles
 */
public interface MethodMutatorFactory extends ClientClasspathPlugin {

  default MethodVisitor create(MutationContext context,
      MethodInfo methodInfo, MethodVisitor methodVisitor) {
    return null;
  }

  @Deprecated
  default AnnotationVisitor createForAnnotation(NoMethodContext context, AnnotationInfo annotationInfo, AnnotationVisitor next) {
    return createForAnnotation((BasicContext) context, annotationInfo, next);
  }
  default AnnotationVisitor createForAnnotation(BasicContext context, AnnotationInfo annotationInfo, AnnotationVisitor next) {
    return null;
  }

  @Deprecated
  default boolean skipAnnotation(NoMethodContext context, AnnotationInfo annotationInfo) {
    return skipAnnotation((BasicContext) context, annotationInfo);
  }

  default boolean skipAnnotation(BasicContext context, AnnotationInfo annotationInfo) {
    return false;
  }

  @Deprecated
  default FieldVisitor createForField(NoMethodContext context, FieldInfo fieldInfo, FieldVisitor fieldVisitor) {
    return createForField((BasicContext) context, fieldInfo, fieldVisitor);
  }

  default FieldVisitor createForField(BasicContext context, FieldInfo fieldInfo, FieldVisitor fieldVisitor) {
    return null;
  }

  String getGloballyUniqueId();

  /**
   * Returns a human readable <code>String</code> representation of this
   * <code>MethodMutatorFactory</code>. The return value of this method will be
   * used in reports to document and describe the mutation(s) applied by the
   * <code>MethodVisitor</code> created by this
   * <code>MethodMutatorFactory</code>.
   *
   * The name is also the key by which mutators are activated and deactivated.
   * More than one MethodMutatorFactory instance might have the same name so
   * they can be activated together. Their globalIds must however be unique.
   *
   * @return a human readable string representation for end-user report
   *         generation.
   */
  String getName();

  @Override
  default String description() {
    return getName();
  }

  default boolean isMutatorFor(MutationIdentifier id) {
    return id.getMutator().equals(getGloballyUniqueId());
  }

}
