package org.pitest.classpath;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;

public class DirectoryClassPathRootTest {

  private DirectoryClassPathRoot testee;

  @Test
  public void getDataShouldReturnNullForUnknownClass() throws Exception {
    this.testee = new DirectoryClassPathRoot(new File("foo"));
    assertThat(this.testee.getData("bar")).isNull();
  }

  @Test
  public void shouldReturnClassNames() {
    final File root = new File("target/test-classes/"); // this is going to be
    // flakey as hell
    this.testee = new DirectoryClassPathRoot(root);
    assertThat(this.testee.classNames()).contains(
        DirectoryClassPathRootTest.class.getName());
  }

}
