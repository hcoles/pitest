package org.pitest.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class PathToJavaClassConverterTest {

  private static final String            SRC    = new File("src/java")
                                                    .getAbsolutePath();
  private final PathToJavaClassConverter testee = new PathToJavaClassConverter(
                                                    SRC);

  @Test
  public void shouldReturnNoMatchedForFilesNotUnderSourceTree() {
    assertFalse(this.testee.apply("not/under/source/tree/File.java").iterator()
        .hasNext());
  }

  @Test
  public void shouldConvertFileInPackageDefaultToJavaClassName() {
    assertEquals("InDefault*", apply("InDefault.java"));
  }

  @Test
  public void shouldConvertFileInPackageToJavaClassName() {
    assertEquals("com.example.Class*", apply("com/example/Class.java"));
  }

  @Test
  public void shouldConvertFilesWithOddCaseExtensionsToJavaClassName() {
    assertEquals("com.example.Class*", apply("com/example/Class.JaVa"));
  }

  @Test
  public void shouldNotConvertFilesWithoutExtension() {
    assertFalse(this.testee.apply(SRC + "/File").iterator().hasNext());
  }

  @Test
  public void shouldConvertFilesWithDotInPath() {
    assertTrue(this.testee.apply(SRC + "/foo.bar/File.java").iterator()
        .hasNext());
  }

  @Test
  public void shouldIncludeWildCardInGeneratedGlobToCatchInnerClasses() {
    assertTrue(apply("foo.java").endsWith("*"));
  }

  @Test
  public void shouldConvertBackslashPathsRegardlessOfOs() {
    assertEquals("com.example.Class*", apply("com\\example\\Class.java"));
  }

  private String apply(final String value) {
    return this.testee.apply(SRC + "/" + value).iterator().next();
  }
}
