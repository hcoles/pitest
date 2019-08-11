/*
 * Copyright 2012 Henry Coles
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
package org.pitest.bytecode;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.objectweb.asm.Opcodes.NOP;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public abstract class MethodDecoratorTest {

  @Mock
  protected MethodVisitor mv;

  protected abstract MethodVisitor getTesteeVisitor();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldForwardVisitCodeCallsToChild() {
    getTesteeVisitor().visitInsn(NOP);
    getTesteeVisitor().visitInsn(Opcodes.ATHROW);
    getTesteeVisitor().visitEnd();
    verify(this.mv).visitCode();
  }

  @Test
  public void shouldForwardVisitEndCallsToChild() {
    getTesteeVisitor().visitInsn(NOP);
    getTesteeVisitor().visitInsn(Opcodes.ATHROW);
    getTesteeVisitor().visitEnd();
    verify(this.mv).visitEnd();
  }

  @Test
  public void shouldForwardVisitAnnotationCallsToChild() {
    getTesteeVisitor().visitAnnotation("foo", true);
    getTesteeVisitor().visitEnd();
    verify(this.mv).visitAnnotation("foo", true);
  }

  @Test
  public void shouldForwardVisitAnnotationDefaultCallsToChild() {
    AnnotationVisitor av = getTesteeVisitor().visitAnnotationDefault();
    if (av != null)
      av.visit("foo", "bar");
    getTesteeVisitor().visitInsn(NOP);
    getTesteeVisitor().visitInsn(Opcodes.ATHROW);
    getTesteeVisitor().visitEnd();
    verify(this.mv).visitAnnotationDefault();
  }

  @Test
  public void shouldForwardVisitAttributeCallsToChild() {
    getTesteeVisitor().visitAttribute(null);
    getTesteeVisitor().visitEnd();
    verify(this.mv).visitAttribute(null);
  }

  @Test
  public void shouldForwardVisitFieldInsnCallsToChild() {
    getTesteeVisitor().visitFieldInsn(1, "2", "3", "4");
    getTesteeVisitor().visitInsn(Opcodes.ATHROW);
    getTesteeVisitor().visitEnd();
    verify(this.mv).visitFieldInsn(1, "2", "3", "4");
  }

  @Test
  public void shouldForwardVisitFrameCallsToChild() {
    final Object[] f1 = { 1, 2 };
    final Object[] f2 = { 2, 4, 6 };
    getTesteeVisitor().visitInsn(NOP);
    getTesteeVisitor().visitFrame(Opcodes.F_FULL, 2, f1, 3, f2);
    getTesteeVisitor().visitEnd();
    verify(this.mv).visitFrame(Opcodes.F_FULL, 2, f1, 3, f2);
  }

  @Test
  public void shouldForwardVisitIincInsnToChild() {
    getTesteeVisitor().visitIincInsn(1, 2);
    getTesteeVisitor().visitInsn(Opcodes.ATHROW);
    getTesteeVisitor().visitEnd();
    verify(this.mv).visitIincInsn(1, 2);
  }

  @Test
  public void shouldForwardVisitInsnToChild() {
    getTesteeVisitor().visitInsn(1);
    getTesteeVisitor().visitInsn(Opcodes.ATHROW);
    getTesteeVisitor().visitEnd();
    verify(this.mv).visitInsn(1);
  }

  @Test
  public void shouldForwardVisitIntInsnToChild() {
    getTesteeVisitor().visitIntInsn(1, 2);
    getTesteeVisitor().visitInsn(Opcodes.ATHROW);
    getTesteeVisitor().visitEnd();
    verify(this.mv).visitIntInsn(1, 2);
  }

  @Test
  public void shouldForwardVisitJumpInsnToChild() {
    final Label l = new Label();
    getTesteeVisitor().visitJumpInsn(1, l);
    getTesteeVisitor().visitEnd();
    verify(this.mv).visitJumpInsn(eq(1), any(Label.class));
  }

  @Test
  public void shouldForwardVisitLabelToChild() {
    final Label l = new Label();
    getTesteeVisitor().visitLabel(l);
    getTesteeVisitor().visitInsn(Opcodes.ATHROW);
    getTesteeVisitor().visitEnd();
    verify(this.mv).visitLabel(any(Label.class));
  }

  @Test
  public void shouldForwardVisitLdcInsnToChild() {
    getTesteeVisitor().visitLdcInsn(1);
    getTesteeVisitor().visitInsn(Opcodes.ATHROW);

    getTesteeVisitor().visitEnd();
    verify(this.mv).visitLdcInsn(1);
  }

  @Test
  public void shouldForwardVisitLineNumberToChild() {
    final Label l = new Label();
    getTesteeVisitor().visitLineNumber(1, l);
    getTesteeVisitor().visitInsn(Opcodes.ATHROW);
    getTesteeVisitor().visitEnd();
    verify(this.mv).visitLineNumber(eq(1), any(Label.class));
  }

  @Test
  public void shouldForwardVisitLocalVariableToChild() {
    final Label l = new Label();
    final Label l2 = new Label();
    getTesteeVisitor().visitCode();
    getTesteeVisitor().visitInsn(NOP);
    getTesteeVisitor().visitLocalVariable("foo", "bar", "one", l, l2, 2);
    getTesteeVisitor().visitInsn(Opcodes.ATHROW);
    getTesteeVisitor().visitEnd();
    verify(this.mv)
        .visitLocalVariable(eq("foo"), eq("bar"), eq("one"), any(Label.class),
            any(Label.class), eq(2));
  }

  @Test
  public void shouldForwardVisitLookupSwitchInsnToChild() {
    final Label l = new Label();
    final Label[] l2 = { new Label() };
    final int[] i = { 1, 2, 3 };
    getTesteeVisitor().visitLookupSwitchInsn(l, i, l2);
    getTesteeVisitor().visitInsn(Opcodes.ATHROW);
    getTesteeVisitor().visitEnd();
    verify(this.mv)
        .visitLookupSwitchInsn(any(Label.class), eq(i), any(Label[].class));
  }

  @Test
  public void shouldForwardVisitMaxsToChild() {
    getTesteeVisitor().visitInsn(NOP);
    getTesteeVisitor().visitInsn(Opcodes.ATHROW);
    getTesteeVisitor().visitMaxs(1, 2);
    getTesteeVisitor().visitEnd();
    verify(this.mv).visitMaxs(1, 2);
  }

  @Test
  public void shouldForwardVisitMethodInsnToChild() {
    getTesteeVisitor().visitMethodInsn(1, "a", "b", "c", false);
    getTesteeVisitor().visitEnd();
    verify(this.mv).visitMethodInsn(1, "a", "b", "c", false);
  }

  @Test
  public void shouldForwardVisitMultiANewArrayInsnToChild() {
    getTesteeVisitor().visitMultiANewArrayInsn("foo", 1);
    getTesteeVisitor().visitEnd();
    verify(this.mv).visitMultiANewArrayInsn("foo", 1);
  }

  @Test
  public void shouldForwardVisitParameterAnnotationToChild() {
    getTesteeVisitor().visitParameterAnnotation(1, "foo", false);
    getTesteeVisitor().visitInsn(NOP);
    getTesteeVisitor().visitInsn(Opcodes.ATHROW);
    getTesteeVisitor().visitEnd();
    verify(this.mv).visitParameterAnnotation(1, "foo", false);
  }

  @Test
  public void shouldForwardVisitTableSwitchInsnToChild() {
    final Label l = new Label();
    final Label[] l2 = { new Label() };
    getTesteeVisitor().visitTableSwitchInsn(1, 2, l, l2);
    getTesteeVisitor().visitInsn(Opcodes.ATHROW);
    getTesteeVisitor().visitEnd();
    verify(this.mv)
        .visitTableSwitchInsn(eq(1), eq(2), any(Label.class), any(Label.class));
  }

  @Test
  public void shouldForwardVisitTryCatchBlockToChild() {
    final Label l = new Label();
    final Label l2 = new Label();
    final Label l3 = new Label();

    getTesteeVisitor().visitTryCatchBlock(l, l2, l3, "foo");
    getTesteeVisitor().visitInsn(NOP);
    getTesteeVisitor().visitInsn(Opcodes.ATHROW);
    getTesteeVisitor().visitEnd();
    verify(this.mv).visitTryCatchBlock(any(Label.class), any(Label.class),
        any(Label.class), eq("foo"));
  }

}
