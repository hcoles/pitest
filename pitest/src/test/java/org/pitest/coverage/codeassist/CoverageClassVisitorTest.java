package org.pitest.coverage.codeassist;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.ClassWriter;
import org.pitest.boot.CodeCoverageStore;
import org.pitest.boot.InvokeReceiver;

public class CoverageClassVisitorTest {

  private CoverageClassVisitor testee;
  private int                  classId;

  @Mock
  private ClassWriter          w;
  
  @Mock
  private InvokeReceiver invokeQueue;
  

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    CodeCoverageStore.init(invokeQueue);
    classId = CodeCoverageStore.registerClass("foo");
    testee = new CoverageClassVisitor(classId, w);
  }

  @Test
  public void shouldRegisterProbesWithCodeCoverageStore() {
    testee.registerLine(2);
    testee.registerLine(12);
    testee.visitEnd();

    CodeCoverageStore.visitLine(CodeCoverageStore.encode(classId, 1));

    assertEquals(Arrays.asList(CodeCoverageStore.encode(classId, 12)),
        CodeCoverageStore.getHits());

  }

}
