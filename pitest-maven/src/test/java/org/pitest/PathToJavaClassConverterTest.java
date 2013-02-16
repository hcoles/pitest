package org.pitest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;

import org.junit.Test;

public class PathToJavaClassConverterTest {
  
  private static final String SRC = new File("src/java").getAbsolutePath();
  private PathToJavaClassConverter testee = new PathToJavaClassConverter(SRC);

  @Test
  public void shouldReturnNoMatchedForFilesNotUnderSourceTree() {
   assertFalse(testee.apply("not/under/source/tree/File.java").iterator().hasNext());
  }

  @Test
  public void shouldConvertFileInPackageDefaultToJavaClassName() {
    assertEquals("InDefault", apply("InDefault.java"));
  }
  
  @Test
  public void shouldConvertFileInPackageToJavaClassName() {
    assertEquals("com.example.Class", apply("com/example/Class.java"));
  }
  
  @Test
  public void shouldConvertFilesWithOddCaseExtensionsToJavaClassName() {
    assertEquals("com.example.Class", apply("com/example/Class.JaVa"));
  }

  private String apply(String value) {
    return testee.apply(SRC + "/" + value).iterator().next();
  }
}
