/*
 * Based on http://code.google.com/p/javacoveragent/ by
 * "alex.mq0" and "dmitry.kandalov"
 * 
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

package org.pitest.coverage.codeassist;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author ivanalx
 * @date 26.01.2009 15:47:10
 */
public class CoverageClassVisitor extends ClassAdapter {
  private final ClassWriter writer;
  private final int         classId;

  private final boolean     allowMethodCoverage;
  private final boolean     allowLineCoverage;

  public CoverageClassVisitor(final int classId, final ClassWriter writer,
      final boolean allowMethodCoverage, final boolean allowLineCoverage) {
    super(writer);

    this.writer = writer;
    this.classId = classId;
    this.allowLineCoverage = allowLineCoverage;
    this.allowMethodCoverage = allowMethodCoverage;
  }

  @Override
  public MethodVisitor visitMethod(final int access, final String name,
      final String desc, final String signature, final String[] exceptions) {
    final MethodVisitor methodVisitor = this.writer.visitMethod(access, name,
        desc, signature, exceptions);
    return new CoverageMethodVisitor(this.classId, methodVisitor, name, desc,
        this.allowMethodCoverage, this.allowLineCoverage);
  }
}
