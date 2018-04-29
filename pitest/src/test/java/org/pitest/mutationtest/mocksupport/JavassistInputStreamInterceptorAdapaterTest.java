package org.pitest.mutationtest.mocksupport;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class JavassistInputStreamInterceptorAdapaterTest {

  private JavassistInputStreamInterceptorMethodVisitor testee;

  @Mock
  private MethodVisitor                                mv;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new JavassistInputStreamInterceptorMethodVisitor(this.mv, "com.example.TheInterceptorClassName");
  }

  @Test
  public void shouldNotInterceptNormalInvokeInterfaceCalls() {
    this.testee.visitMethodInsn(Opcodes.INVOKEINTERFACE, "foo", "bar", "far",
        false);
    verify(this.mv).visitMethodInsn(Opcodes.INVOKEINTERFACE, "foo", "bar",
        "far", false);
  }

  @Test
  public void shouldNotInterceptNormalInvokeStaticCalls() {
    this.testee.visitMethodInsn(Opcodes.INVOKESTATIC, "foo", "bar", "far",
        false);
    verify(this.mv).visitMethodInsn(Opcodes.INVOKESTATIC, "foo", "bar", "far",
        false);
  }

  @Test
  public void shouldNotInterceptCallsToMethodsCalledOpenClassFileNotInJavaAssist() {
    this.testee.visitMethodInsn(Opcodes.INVOKEINTERFACE, "foo",
        "openClassfile", "far", false);
    verify(this.mv).visitMethodInsn(Opcodes.INVOKEINTERFACE, "foo",
        "openClassfile", "far", false);
  }

  @Test
  public void shouldInterceptCallsToOpenClassFileInJavaAssist() {
    this.testee.visitMethodInsn(Opcodes.INVOKEINTERFACE, "javassist/ClassPath",
        "openClassfile", "far", false);
    verify(this.mv).visitMethodInsn(Opcodes.INVOKESTATIC,
        "com.example.TheInterceptorClassName",
        "openClassfile",
        "(Ljava/lang/Object;Ljava/lang/String;)Ljava/io/InputStream;", false);
  }
}
