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

package org.pitest.coverage;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.classinfo.BridgeMethodFilter;
import org.pitest.classinfo.MethodFilteringAdapter;
import org.pitest.coverage.analysis.CoverageAnalyser;
import sun.pitest.CodeCoverageStore;

/**
 * Instruments a class with probes on each line
 */
public class CoverageClassVisitor extends MethodFilteringAdapter {
  private final int classId;

  /**
   * Probe count starts at 1, because probe "0" indicates that the class was hit
   * by this test.
   */
  private int       probeCount = 1;

  private String    className;
  private boolean   foundClinit;

  public CoverageClassVisitor(final int classId, final ClassWriter writer) {
    super(writer, BridgeMethodFilter.INSTANCE);
    this.classId = classId;
  }

  public void registerProbes(final int number) {
    this.probeCount = this.probeCount + number;
  }

  @Override
  public void visit(int version, int access, String name, String signature,
      String superName, String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces);
    this.className = name;
  }


  @Override
  public MethodVisitor visitMethodIfRequired(final int access,
      final String name, final String desc, final String signature,
      final String[] exceptions, final MethodVisitor methodVisitor) {

    if (name.equals("<clinit>")) {
      foundClinit = true;
    }

    return new CoverageAnalyser(this, this.classId, this.probeCount,
        methodVisitor, access, name, desc, signature, exceptions);

  }

  @Override
  public FieldVisitor visitField(int access, String name, String descriptor,
      String signature, Object value) {

    /*
    If this class was already instrumented, then do not do it again!
     */
    if (name.equals(CodeCoverageStore.PROBE_FIELD_NAME)) {
      throw new AlreadyInstrumentedException("Class " + getClassName()
          + " already has coverage instrumentation, but asked to do it again!");
    }
    return super.visitField(access, name, descriptor, signature, value);
  }

  @Override
  public void visitEnd() {
    addCoverageProbeField();
  }

  private void addCoverageProbeField() {

    super.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_PUBLIC
            | Opcodes.ACC_SYNTHETIC, CodeCoverageStore.PROBE_FIELD_NAME, "[Z", null,
        null);

    super.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_PUBLIC
            | Opcodes.ACC_SYNTHETIC, CodeCoverageStore.PROBE_LENGTH_FIELD_NAME, "I",
        null, this.probeCount + 1);

    //If there is no <clinit>, then generate one that sets the probe field directly
    if (!foundClinit) {
      MethodVisitor clinitMv = this.cv
          .visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
      clinitMv.visitCode();


      pushConstant(clinitMv, this.classId);
      pushConstant(clinitMv, this.probeCount);
      clinitMv
          .visitMethodInsn(Opcodes.INVOKESTATIC, CodeCoverageStore.CLASS_NAME,
              "getOrRegisterClassProbes", "(II)[Z", false);

      clinitMv.visitFieldInsn(Opcodes.PUTSTATIC, className,
          CodeCoverageStore.PROBE_FIELD_NAME, "[Z");
      clinitMv.visitInsn(Opcodes.RETURN);
      clinitMv.visitMaxs(0, 0);
      clinitMv.visitEnd();
    }
  }

  private void pushConstant(MethodVisitor mv, int value) {
    switch (value) {
      case 0:
        mv.visitInsn(Opcodes.ICONST_0);
        break;
      case 1:
        mv.visitInsn(Opcodes.ICONST_1);
        break;
      case 2:
        mv.visitInsn(Opcodes.ICONST_2);
        break;
      case 3:
        mv.visitInsn(Opcodes.ICONST_3);
        break;
      case 4:
        mv.visitInsn(Opcodes.ICONST_4);
        break;
      case 5:
        mv.visitInsn(Opcodes.ICONST_5);
        break;
      default:
        if (value <= Byte.MAX_VALUE) {
          mv.visitIntInsn(Opcodes.BIPUSH, value);
        } else if (value <= Short.MAX_VALUE) {
          mv.visitIntInsn(Opcodes.SIPUSH, value);
        } else {
          mv.visitLdcInsn(value);
        }
    }
  }

  public String getClassName() {
    return className;
  }
}
