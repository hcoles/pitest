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

import org.objectweb.asm.MethodVisitor;

/**
 * A <code>MethodMutatorFactory</code> is a factory creating method mutating
 * method visitors. Those method visitors will serve two purposes: finding new
 * mutation points (locations in byte code where mutations can be applied) and
 * applying those mutations to the byte code.
 *
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
public interface MethodMutatorFactory {

  MethodVisitor create(MutationContext context,
      MethodInfo methodInfo, MethodVisitor methodVisitor);

  String getGloballyUniqueId();

  /**
   * Returns a human readable <code>String</code> representation of this
   * <code>MethodMutatorFactory</code>. The return value of this method will be
   * used in reports to document and describe the mutation(s) applied by the
   * <code>MethodVisitor</code> created by this
   * <code>MethodMutatorFactory</code>.
   *
   *
   * @return a human readable string representation for end-user report
   *         generation.
   */
  String getName();

}
