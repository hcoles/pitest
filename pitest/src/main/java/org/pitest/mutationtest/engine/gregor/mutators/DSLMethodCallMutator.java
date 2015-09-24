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

package org.pitest.mutationtest.engine.gregor.mutators;

import org.objectweb.asm.MethodVisitor;
import org.pitest.functional.F2;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

public enum DSLMethodCallMutator implements MethodMutatorFactory {

  DSL_METHOD_CALL_MUTATOR;

  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new MethodCallMethodVisitor(methodInfo, context, methodVisitor,
        this, dslMethods()) {
      @Override
      protected boolean filter(int opcode, String owner, String name, String desc, boolean itf) {
        return super.filter(opcode, owner, name, desc, itf) 
        		&& MethodInfo.doesReturnOwner(owner,desc);
      }
    };
  }

  public String getGloballyUniqueId() {
    return this.getClass().getName();
  }

  public String getName() {
    return name();
  }

  private F2<String, String, Boolean> dslMethods() {
    return new F2<String, String, Boolean>() {

      public Boolean apply(final String name, final String desc) {
        return true;
      }

    };
  }

}
