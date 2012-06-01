package org.pitest.mutationtest.mocksupport;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.mockito.Mockito.*;

public class JavassistInputStreamInterceptorAdapaterTest {

  private JavassistInputStreamInterceptorMethodVisitor testee;

  @Mock
  private MethodVisitor                                mv;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    testee = new JavassistInputStreamInterceptorMethodVisitor(mv);
  }

  @Test
  public void shouldNotInterceptNormalInvokeInterfaceCalls() {
    testee.visitMethodInsn(Opcodes.INVOKEINTERFACE, "foo", "bar", "far");
    verify(mv).visitMethodInsn(Opcodes.INVOKEINTERFACE, "foo", "bar", "far");
  }
  
  @Test
  public void shouldNotInterceptNormalInvokeStaticCalls() {
    testee.visitMethodInsn(Opcodes.INVOKESTATIC, "foo", "bar", "far");
    verify(mv).visitMethodInsn(Opcodes.INVOKESTATIC, "foo", "bar", "far");
  }
  
  @Test
  public void shouldNotInterceptCallsToMethodsCalledOpenClassFileNotInJavaAssist() {
    testee.visitMethodInsn(Opcodes.INVOKEINTERFACE, "foo",
        "openClassfile", "far");
    verify(mv).visitMethodInsn(Opcodes.INVOKEINTERFACE, "foo", "openClassfile", "far");
  }

  @Test
  public void shouldInterceptCallsToOpenClassFileInJavaAssist() {
    testee.visitMethodInsn(Opcodes.INVOKEINTERFACE, "javassist/ClassPath",
        "openClassfile", "far");
    verify(mv).visitMethodInsn(Opcodes.INVOKESTATIC,
        "org/pitest/mutationtest/mocksupport/JavassistInterceptor",
        "openClassfile",
        "(Ljava/lang/Object;Ljava/lang/String;)Ljava/io/InputStream;");
  }
}
