package org.pitest.coverage.codeassist;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Need to count the number of lines in the method. Storing method as a tree
 * enables a second scan by the instrumenting visitor
 * 
 */
public class CoverageAnalyser extends MethodNode {

  private final CoverageClassVisitor cv;
  private final int                  classId;
  private final MethodVisitor        mv;

  public CoverageAnalyser(final CoverageClassVisitor cv, final int classId,
      final MethodVisitor mv, final int access, final String name,
      final String desc, final String signature, final String[] exceptions) {
    super(access, name, desc, signature, exceptions);
    this.mv = mv;
    this.cv = cv;
    this.classId = classId;
  }

  @Override
  public void visitEnd() {
    final int numberOfLines = countRequiredProbes();
    accept(new CoverageMethodVisitor(this.cv, this.classId, this.mv,
        this.access, this.name, this.desc, numberOfLines));
  }

  private int countRequiredProbes() {
    int count = 0;
    for (int i = 0; i < this.instructions.size(); i++) {
      final AbstractInsnNode ins = this.instructions.get(i);
      if (ins instanceof LineNumberNode) {
        count++;

      }
    }
    return count;
  }
}
