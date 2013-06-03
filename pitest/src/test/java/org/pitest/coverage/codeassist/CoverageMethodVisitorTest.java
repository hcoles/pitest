package org.pitest.coverage.codeassist;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.objectweb.asm.MethodVisitor;
import org.pitest.bytecode.MethodDecoratorTest;

public class CoverageMethodVisitorTest extends MethodDecoratorTest {

  private CoverageMethodVisitor testee;

  @Mock
  private CoverageClassVisitor  cv;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    this.testee = new CoverageMethodVisitor(this.cv, 0, this.mv, 0, "name",
        "()V", 0, 0);
  }

  @Override
  protected MethodVisitor getTesteeVisitor() {
    return this.testee;
  }

  @Test
  public void shouldRegisterLinesWithCoverageClassVisitor() {
    this.testee.visitLineNumber(42, null);
    verify(this.cv).registerLine(42);
  }

  @Override
  public void shouldForwardVisitFrameCallsToChild() {
    // pass - adviceadapter cannot be called with visit frames
  }

  @Override
  public void shouldForwardVisitLocalVariableToChild() {
    // pass
  }

  @Override
  public void shouldForwardVisitMaxsToChild() {
    // pass
  }

}
