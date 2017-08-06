package org.pitest.mutationtest.engine.gregor.analysis;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.util.IsolationUtils;

public class InstructionTrackingMethodVisitorTest {

  private final ClassByteArraySource byteSource = new ClassloaderByteArraySource(
      IsolationUtils
      .getContextClassLoader());

  private final InstructionCounter   counter    = new DefaultInstructionCounter();

  @Test
  public void shouldGiveIndexConsistentWithTreeApiForStringEquals() {
    analyse(String.class, "equals");
    final MethodNode tree = makeTree(String.class, "equals");
    assertEquals(tree.instructions.size(),
        this.counter.currentInstructionCount());
  }

  class HasMethodCallsAndBranches {
    public int foo(final long j) {
      if (j > 64000) {
        bar();
      }
      return 32;
    }

    private void bar() {

    }
  }

  @Test
  public void shouldGiveIndexConsistentWithTreeApiWhenMethodCallsPresent() {
    analyse(HasMethodCallsAndBranches.class, "foo");
    final MethodNode tree = makeTree(HasMethodCallsAndBranches.class, "foo");
    assertEquals(tree.instructions.size(),
        this.counter.currentInstructionCount());
  }

  class HasSwitchStatements {
    public int foo(final int j) {
      switch (j) {
      case 1:
        return 3;
      case 2:
        return 4;
      }

      switch (j) {
      case 34:
        return 1;
      case 9:
        return 2;
      default:
        return 6;
      }
    }

  }

  @Test
  public void shouldGiveIndexConsistentWithTreeApiWhenSwitchStatementsPresent() {
    analyse(HasSwitchStatements.class, "foo");
    final MethodNode tree = makeTree(HasSwitchStatements.class, "foo");
    assertEquals(tree.instructions.size(),
        this.counter.currentInstructionCount());
  }

  private InstructionTrackingMethodVisitor analyse(final Class<?> clazz,
      final String targetMethod) {
    final ClassReader reader = new ClassReader(this.byteSource.getBytes(
        clazz.getName()).value());
    final Analyser cv = new Analyser(targetMethod);
    reader.accept(cv, 0);
    return cv.testee;
  }

  private MethodNode makeTree(final Class<?> clazz, final String name) {
    final ClassReader reader = new ClassReader(this.byteSource.getBytes(
        ClassName.fromClass(clazz).asJavaName()).value());
    final ClassNode tree = new ClassNode();
    reader.accept(tree, 0);
    for (Object m : tree.methods) {
      MethodNode mn = (MethodNode) m;
      if (mn.name.equals(name)) {
        return mn;
      }
    }
    throw new RuntimeException("Method " + name + " not found in " + clazz);
  }

  private class Analyser extends ClassVisitor {
    private final String             targetMethod;
    InstructionTrackingMethodVisitor testee;

    public Analyser(final String targetMethod) {
      super(Opcodes.ASM6);
      this.targetMethod = targetMethod;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
        final String desc, final String signature, final String[] exceptions) {
      if (name.equals(this.targetMethod)) {
        this.testee = new InstructionTrackingMethodVisitor(super.visitMethod(
            access, name, desc, signature, exceptions),
            InstructionTrackingMethodVisitorTest.this.counter);
        return this.testee;
      } else {
        return null;
      }
    }

  }

}
