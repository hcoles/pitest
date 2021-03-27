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

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.pitest.mutationtest.engine.gregor.analysis.InstructionCounter;
import sun.pitest.CodeCoverageStore;

import java.util.List;

/**
 * Instruments a method adding probes at each block. Assumes array probe is final
 * and has always been initialised.
 */
public class ArrayProbeCoverageMethodVisitor extends AbstractCoverageStrategy {

    private int probeHitArrayLocal;

    public ArrayProbeCoverageMethodVisitor(List<Block> blocks,
                                           InstructionCounter counter,
                                           int classId,
                                           MethodVisitor writer,
                                           int access,
                                           String className,
                                           String name,
                                           String desc,
                                           int probeOffset) {
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
            this.mv.visitFieldInsn(Opcodes.GETSTATIC, this.className, CodeCoverageStore.PROBE_LENGTH_FIELD_NAME, "I");
            this.mv
                    .visitMethodInsn(Opcodes.INVOKESTATIC, CodeCoverageStore.CLASS_NAME,
                            "getOrRegisterClassProbes", "(II)[Z", false);
            this.mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
                    CodeCoverageStore.PROBE_FIELD_NAME, "[Z");
        }
        this.probeHitArrayLocal = newLocal(Type.getType("[Z"));

        this.mv.visitFieldInsn(Opcodes.GETSTATIC, className,
                CodeCoverageStore.PROBE_FIELD_NAME, "[Z");

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
