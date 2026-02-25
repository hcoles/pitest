package org.pitest.maven;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;

public class PathToJavaClassConverterTest {

  private static final String            SRC    = new File("src/java")
                                                    .getAbsolutePath();
  private final PathToJavaClassConverter testee = new PathToJavaClassConverter(
                                                    SRC);

  @Test
  public void shouldReturnNoMatchedForFilesNotUnderSourceTree() {
    assertThat(this.testee.apply("not/under/source/tree/File.java").iterator()
        .hasNext()).isFalse();
  }

  @Test
  public void shouldConvertFileInPackageDefaultToJavaClassName() {
    assertThat(apply("InDefault.java")).isEqualTo("InDefault*");
  }

  @Test
  public void shouldConvertFileInPackageToJavaClassName() {
    assertThat(apply("com/example/Class.java")).isEqualTo("com.example.Class*");
  }

  @Test
  public void shouldConvertFilesWithOddCaseExtensionsToJavaClassName() {
    assertThat(apply("com/example/Class.JaVa")).isEqualTo("com.example.Class*");
  }

  @Test
  public void shouldNotConvertFilesWithoutExtension() {
    assertThat(this.testee.apply(SRC + "/File").iterator().hasNext()).isFalse();
  }

  @Test
  public void shouldConvertFilesWithDotInPath() {
    assertThat(this.testee.apply(SRC + "/foo.bar/File.java").iterator()
        .hasNext()).isTrue();
  }

  @Test
  public void shouldIncludeWildCardInGeneratedGlobToCatchInnerClasses() {
    assertThat(apply("foo.java")).endsWith("*");
  }

  @Test
  public void shouldConvertBackslashPathsRegardlessOfOs() {
    assertThat(apply("com\\example\\Class.java")).isEqualTo("com.example.Class*");
  }

  private String apply(final String value) {
    return this.testee.apply(SRC + "/" + value).iterator().next();
  }
}
