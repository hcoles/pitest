package org.pitest.coverage.codeassist;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.ClassWriter;
import org.pitest.coverage.CoverageClassVisitor;

import sun.pitest.CodeCoverageStore;
import sun.pitest.InvokeReceiver;

public class CoverageClassVisitorTest {

  private CoverageClassVisitor testee;
  private int                  classId;

  @Mock
  private ClassWriter          w;

  @Mock
  private InvokeReceiver       invokeQueue;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    CodeCoverageStore.init(this.invokeQueue);
    CodeCoverageStore.reset();
    this.classId = CodeCoverageStore.registerClass("foo");
    this.testee = new CoverageClassVisitor(this.classId, this.w);
  }

  @Test
  public void shouldRegisterProbesWithCodeCoverageStore() {
    this.testee.registerProbes(6);
    this.testee.visitEnd();

    CodeCoverageStore.visitProbes(this.classId, 0, new boolean[] { false,
        false, true, false, false, false, false });

    assertEquals(Arrays.asList(CodeCoverageStore.encode(this.classId, 2)),
        CodeCoverageStore.getHits());

  }

}
