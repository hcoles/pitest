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

package org.pitest.coverage.analysis;

import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.pitest.mutationtest.engine.gregor.analysis.InstructionCounter;

import sun.pitest.CodeCoverageStore;

/**
 * Instruments a method adding probes at each line. The strategy requires the
 * compiler to be configured to add line number debug information.
 *
 * Probes are implemented by adding an array to each method. Lines hits are
 * registered by a write to this local array. Each method exit point is then
 * augmented with a call that passes this array to the coverage store class that
 * handles communication of this data back to the parent process on the
 * completion of each test.
 *
 * All methods are wrapped in a try finally block to ensure that coverage data
 * is sent in the event of a runtime exception.
 *
 * Creating a new array on each method entry is not cheap - other coverage
 * systems add a static field used across all methods. We must clear down all
 * coverage history for each test however. Resetting static fields in all loaded
 * classes would be messy to implement - it may or may not be faster than the
 * current approach.
 */
public class ArrayProbeCoverageMethodVisitor extends AbstractCoverageStrategy {

  private int probeHitArrayLocal;

  public ArrayProbeCoverageMethodVisitor(List<Block> blocks,
      InstructionCounter counter, final int classId,
      final MethodVisitor writer, final int access, final String name,
      final String desc, final int probeOffset) {
    super(blocks, counter, classId, writer, access, name, desc, probeOffset);
  }

  @Override
  void prepare() {
    this.probeHitArrayLocal = newLocal(Type.getType("[Z"));

    pushConstant(this.blocks.size());
    this.mv.visitIntInsn(NEWARRAY, T_BOOLEAN);
    this.mv.visitVarInsn(ASTORE, this.probeHitArrayLocal);
  }

  @Override
  void generateProbeReportCode() {

    pushConstant(this.classId);
    pushConstant(this.probeOffset);
    this.mv.visitVarInsn(ALOAD, this.probeHitArrayLocal);

    this.methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
        CodeCoverageStore.CLASS_NAME, CodeCoverageStore.PROBE_METHOD_NAME,
        "(II[Z)V", false);
  }

  @Override
  void insertProbe() {
    this.mv.visitVarInsn(ALOAD, this.probeHitArrayLocal);
    pushConstant(this.probeCount);
    pushConstant(1);
    this.mv.visitInsn(BASTORE);
  }

}
