package org.pitest.mutationtest.decompilation;

import java.util.List;

import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;

import static org.assertj.core.api.Assertions.assertThat;

public class DecompilerTest {

  @Test
  public void shouldDecompileJava() {
    Decompiler testee = new Decompiler(new ClassloaderByteArraySource());
    List<String> actual = testee.decompile(ClassName.fromClass(DecompilerTest.class));
    
    assertThat(actual).contains("public class DecompilerTest");   
  }

}
