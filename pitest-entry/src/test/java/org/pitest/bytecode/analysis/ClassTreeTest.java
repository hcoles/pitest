package org.pitest.bytecode.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Function;

import org.junit.Test;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classpath.ClassloaderByteArraySource;

public class ClassTreeTest {

  private final ClassByteArraySource source = ClassloaderByteArraySource.fromContext();

  @Test
  public void shouldCreateTreeFromValidByteArray() {
    final ClassTree testee = ClassTree.fromBytes(bytesFor(String.class));
    assertThat(testee.rawNode().name).isEqualTo("java/lang/String");
  }

  @Test
  public void shouldAllowAccesToAllMethods() {
    final ClassTree testee = ClassTree.fromBytes(bytesFor(ParseMe.class));
    assertThat(testee.methods().stream().map(toName())).containsExactly("<init>", "a", "b");
  }

  @Test
  public void toStringShouldPrintBytecode() {
    final ClassTree testee = ClassTree.fromBytes(bytesFor(ParseMe.class));
    assertThat(testee.toString()).contains("ALOAD 0");
  }

  byte[] bytesFor(Class<?> clazz) {
    return this.source.getBytes(clazz.getName()).get();
  }

 private static Function<MethodTree,String> toName() {
  return a -> a.rawNode().name;

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