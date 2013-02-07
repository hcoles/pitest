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
    this.testee = new CoverageMethodVisitor(this.cv, 0, this.mv);
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

}
