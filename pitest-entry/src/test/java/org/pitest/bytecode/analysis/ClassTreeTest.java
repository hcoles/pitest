package org.pitest.bytecode.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.functional.F;

public class ClassTreeTest {
  
  private ClassByteArraySource source = ClassloaderByteArraySource.fromContext();

  @Test
  public void shouldCreateTreeFromValidByteArray() {
    ClassTree testee = ClassTree.fromBytes(bytesFor(String.class));
    assertThat(testee.rawNode().name).isEqualTo("java/lang/String");
  }
  
  @Test
  public void shouldAllowAccesToAllMethods() {
    ClassTree testee = ClassTree.fromBytes(bytesFor(ParseMe.class));
    assertThat(testee.methods().map(toName())).containsExactly("<init>", "a", "b");
  }
  
  @Test
  public void toStringShouldPrintBytecode() {
    ClassTree testee = ClassTree.fromBytes(bytesFor(ParseMe.class));
    assertThat(testee.toString()).contains("ALOAD 0");
  }
  
  byte[] bytesFor(Class<?> clazz) {
    return source.getBytes(clazz.getName()).value();
  }

 private static F<MethodTree,String> toName() {
  return new  F<MethodTree,String>() {
    @Override
    public String apply(MethodTree a) {
      return a.rawNode().name;
    }
  };
   
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