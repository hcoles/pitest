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
package org.pitest.classinfo;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.pitest.bytecode.ASMVersion;
import org.pitest.functional.F5;

public abstract class MethodFilteringAdapter extends ClassVisitor {

  private final F5<Integer, String, String, String, String[], Boolean> filter;

  public MethodFilteringAdapter(final ClassVisitor writer,
      final F5<Integer, String, String, String, String[], Boolean> filter) {
    super(ASMVersion.ASM_VERSION, writer);
    this.filter = filter;
  }

  private boolean shouldInstrument(final int access, final String name,
      final String desc, final String signature, final String[] exceptions) {
    return this.filter.apply(access, name, desc, signature, exceptions);
  }

  @Override
  public final MethodVisitor visitMethod(final int access, final String name,
      final String desc, final String signature, final String[] exceptions) {
    final MethodVisitor methodVisitor = this.cv.visitMethod(access, name, desc,
        signature, exceptions);
    if (shouldInstrument(access, name, desc, signature, exceptions)) {
      return visitMethodIfRequired(access, name, desc, signature, exceptions,
          methodVisitor);
    } else {
      return methodVisitor;
    }
  }

  public abstract MethodVisitor visitMethodIfRequired(int access, String name,
      String desc, String signature, String[] exceptions,
      MethodVisitor methodVisitor);

}
