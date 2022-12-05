package org.pitest.bytecode.analysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

import org.junit.Test;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.coverage.codeassist.samples.Bridge;
import org.pitest.coverage.codeassist.samples.HasDefaultConstructor;
import org.pitest.coverage.codeassist.samples.NoDefaultConstructor;

public class ClassTreeTest {

  private final ClassByteArraySource source = ClassloaderByteArraySource.fromContext();

  @Test
  public void shouldCreateTreeFromValidByteArray() {
    final ClassTree testee = ClassTree.fromBytes(bytesFor(String.class));
    assertThat(testee.rawNode().name).isEqualTo("java/lang/String");
  }

  @Test
  public void shouldAllowAccessToAllMethods() {
    final ClassTree testee = ClassTree.fromBytes(bytesFor(ParseMe.class));
    assertThat(testee.methods().stream().map(a -> a.rawNode().name)).containsExactly("<init>", "a", "b");
  }

  @Test
  public void toStringShouldPrintBytecode() {
    final ClassTree testee = ClassTree.fromBytes(bytesFor(ParseMe.class));
    assertThat(testee.toString()).contains("ALOAD 0");
  }

  @Test
  public void shouldDetectStandardCodeLines()  {
    ClassTree underTest = ClassTree.fromBytes(bytesFor(NoDefaultConstructor.class));
    assertThat(underTest.codeLineNumbers()).contains(25);
  }

  @Test
  public void lineCountIsCorrectWhenLineNumbersRepeated() {
    ClassTree underTest = ClassTree.fromBytes(bytesFor(RepeatedLineNumbersInByteCode.class));
    assertThat(underTest.numberOfCodeLines()).isEqualTo(5);
  }

  @Test
  public void shouldDetectCodeLineAtClassDeclarationsWhenClassHasDefaultConstructor() {
    ClassTree underTest = ClassTree.fromBytes(bytesFor(HasDefaultConstructor.class));
    assertThat(underTest.codeLineNumbers()).contains(17);
    assertThat(underTest.codeLineNumbers()).doesNotContain(16);
  }

  @Test
  public void shouldNotDetectCodeLineAtClassDeclarationsWhenClassHasNoDefaultConstructor() {
    ClassTree underTest = ClassTree.fromBytes(bytesFor(NoDefaultConstructor.class));
    assertThat(underTest.codeLineNumbers())
            .withFailMessage("first line of class without default constructor should not be a code line")
            .doesNotContain(17);
  }

  @Test
  public void shouldNotRecordLineNumbersFromSyntheticBridgeMethods() {
    ClassTree underTest = ClassTree.fromBytes(bytesFor(Bridge.HasBridgeMethod.class));
    assertThat(underTest.codeLineNumbers()).doesNotContain(24);
  }

  @Test
  public void shouldNotIncludeBridgeMethodsInCodeLineCount() {
    ClassTree underTest = ClassTree.fromBytes(bytesFor(Bridge.HasBridgeMethod.class));
    assertThat(underTest.numberOfCodeLines()).isEqualTo(3);
  }

  @Test
  public void realMethodsDoesNotIncludeBridgeMethods() {
    ClassTree underTest = ClassTree.fromBytes(bytesFor(Bridge.HasBridgeMethod.class));
    assertThat(underTest.realMethods()).hasSize(underTest.methods().size() - 1);
  }

  @Test
  public void realMethodsDoesNotIncludeSynthetics() {
    ClassTree underTest = ClassTree.fromBytes(bytesFor(ParseMe.class));
    MethodTree method = underTest.methods().get(0);
    method.rawNode().access |= ACC_SYNTHETIC;
    assertThat(underTest.realMethods()).doesNotContain(method);
  }

  @Test
  public void realMethodsIncludesSyntheticsGeneratedForLambdas() {
    ClassTree underTest = ClassTree.fromBytes(bytesFor(ParseMe.class));
    MethodTree method = underTest.methods().get(0);
    method.rawNode().access |= ACC_SYNTHETIC;
    method.rawNode().name = "lambda$something";
    assertThat(underTest.realMethods()).contains(method);
  }
  byte[] bytesFor(Class<?> clazz) {
    return this.source.getBytes(clazz.getName()).get();
  }

}

class ParseMe {
  public void a() {
    b();
  }

  private int b() {
    return 0;
  }
}

class RepeatedLineNumbersInByteCode {
  public boolean a(int b) {
    try {
      Integer.valueOf("" + b);
      System.out.println(b);
    } finally {
      System.out.println(b + 1);
      return b > 10;
    }
  }
}