package org.pitest.coverage.codeassist;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.ClassWriter;

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
    this.testee.registerLine(2);
    this.testee.registerLine(12);
    this.testee.visitEnd();

    CodeCoverageStore.visitProbes(this.classId, 0,
        new boolean[] { false, true });

    assertEquals(Arrays.asList(CodeCoverageStore.encode(this.classId, 12)),
        CodeCoverageStore.getHits());

  }

}
