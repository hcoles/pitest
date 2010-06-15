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
package org.pitest.internal.transformation;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.pitest.extension.Transformation;

public class EnvironmentAccessTransformation implements Transformation {

  public byte[] transform(final String name, final byte[] bytes) {
    final ClassReader cr = new ClassReader(bytes);
    final ClassWriter cw = new ClassWriter(cr, 0);
    final ClassAdapter ca = new EnvironmentAccessClassAdapter(cw);
    cr.accept(ca, 0);
    return cw.toByteArray();
  }

}
