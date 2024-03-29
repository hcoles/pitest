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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.pitest.mutationtest.engine.gregor.analysis.InstructionCounter;
import sun.pitest.CodeCoverageStore;

import java.util.List;

/**
 *
 * Instruments a method adding probes at each block.
 *
 * <p>Probes are implemented by adding an array to each class. Block hits are
 * registered by a write to this local array. The array is registered upon class
 * initialization with the {@code CodeCoverageStore}, and all methods in the same class
 * share the same array. The coverage store class reads this array at the end of
 * the test and handles communication of this data back to the parent process.
 *
 * <p>The old approach was to allocate an array in *each* invocation of each method,
 * and merge this in to a global array, which could get flushed between test runs.
 * The approach implemented here requires far fewer allocations and is faster, plus
 * it's better from a concurrency perspective (no locking needed except when first
 * initializing the coverage probe array).
 *
 * <p>Here's a source-level example of the instrumentation result:
 *
 * <pre>
 * public class Foo {
 *   private static int $$pitCoverageProbeSize = 10; //how many blocks there are + 1
 *   private static boolean[] $$pitCoverageProbes = CodeCoverageStore.getOrRegisterClassProbes(thisClassID, $$pitCoverageProbeSize);
 *
 *   private void bar(){
 *     boolean[] localRefToProbes = $$pitCoverageProbes;
 *     if (localRefToProbes == null) {
 *         localRefToProbes = $$pitCoverageProbes = CodeCoverageStore.getOrRegisterClassProbes(thisClassID, $$pitCoverageProbeSize);
 *     }
 *     localRefToProbes[0] = true; //record class was hit
 *     //line of code
 *     localRefToProbes[1] = true; //assuming above line was probe 1
 *   }
 * }
 * </pre>
 *
 * <p>{@code CodeCoverageStore} maintains a reference to all of these {@code $$pitCoverageProbes} arrays
 * and empties them out between each test.
 */
public class ArrayProbeCoverageMethodVisitor extends AbstractCoverageStrategy {

  private int           probeHitArrayLocal;

  public ArrayProbeCoverageMethodVisitor(List<Block> blocks,
      InstructionCounter counter, final int classId,
      final MethodVisitor writer, final int access, final String className, final String name,
      final String desc, final int probeOffset) {
    super(blocks, counter, classId, writer, access, className, name, desc, probeOffset);
  }

  @Override
  public void visitMethodInsn(int opcode, String owner, String name,
      String desc, boolean itf) {

    super.visitMethodInsn(opcode, owner, name, desc, itf);
  }

  @Override
  public void visitFieldInsn(int opcode, String owner, String name,
      String desc) {
    super.visitFieldInsn(opcode, owner, name, desc);
  }

  @Override
  void prepare() {
    if (getName().equals("<clinit>")) {
        pushConstant(this.classId);
        this.mv.visitFieldInsn(Opcodes.GETSTATIC, this.className, CodeCoverageStore.PROBE_LENGTH_FIELD_NAME,"I");
        this.mv
            .visitMethodInsn(Opcodes.INVOKESTATIC, CodeCoverageStore.CLASS_NAME,
                "getOrRegisterClassProbes", "(II)[Z", false);
        this.mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
            CodeCoverageStore.PROBE_FIELD_NAME, "[Z");
    }
    this.probeHitArrayLocal = newLocal(Type.getType("[Z"));

    this.mv.visitFieldInsn(Opcodes.GETSTATIC, className,
        CodeCoverageStore.PROBE_FIELD_NAME, "[Z");

    this.mv.visitInsn(DUP); //duplicate array reference, one for null check and one to use

    //Check if PROBE_FIELD_NAME has been initialised
    Label notnull = new Label();
    this.mv.visitJumpInsn(Opcodes.IFNONNULL,notnull);

    //if not then initialise
    this.mv.visitInsn(POP); //gte rid of null on top of stack
    pushConstant(this.classId);
    this.mv.visitFieldInsn(Opcodes.GETSTATIC, this.className, CodeCoverageStore.PROBE_LENGTH_FIELD_NAME,"I");
    this.mv
            .visitMethodInsn(Opcodes.INVOKESTATIC, CodeCoverageStore.CLASS_NAME,
                    "getOrRegisterClassProbes", "(II)[Z", false);
    this.mv.visitInsn(DUP);//duplicate array reference, one to store and one to use
    this.mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
            CodeCoverageStore.PROBE_FIELD_NAME, "[Z");

    //else do nothing
    this.mv.visitLabel(notnull);

    //Make sure that we recorded that the class was hit
    this.mv.visitInsn(DUP);
    this.mv.visitInsn(ICONST_0);
    this.mv.visitInsn(ICONST_1);
    this.mv.visitInsn(BASTORE);
    this.mv.visitVarInsn(ASTORE, this.probeHitArrayLocal);
  }

  @Override
  void generateProbeReportCode() {
  }

  @Override
  void insertProbe() {
    this.mv.visitVarInsn(ALOAD, this.probeHitArrayLocal);
    pushConstant(this.probeOffset + this.probeCount);
    this.mv.visitInsn(ICONST_1);
    this.mv.visitInsn(BASTORE);
  }

}
