package org.pitest.bytecode.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Function;

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
    assertThat(underTest.codeLineNumbers()).doesNotContain(1);
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