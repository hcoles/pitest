package org.pitest.maven;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;


@Category(SystemTest.class)
public class NonEmptyProjectCheckIT {

  @Rule
  public TemporaryFolder realDir = new TemporaryFolder();

  @Rule
  public TemporaryFolder emptyDir = new TemporaryFolder();
  
  NonEmptyProjectCheck testee = new NonEmptyProjectCheck();

  private MavenProject project = Mockito.mock(MavenProject.class);

  @Before
  public void populateDirectory() throws IOException {
    realDir.newFile("temp");
  }

  @Test
  public void shouldTreatProjectWithCodeAndTestsAsNonEmpty() {
    when(project.getTestCompileSourceRoots()).thenReturn(Collections.singletonList(dirWithContents()));
    when(project.getCompileSourceRoots()).thenReturn(Collections.singletonList(dirWithContents()));
    assertThat(testee.test(project)).isTrue();
  }


  @Test
  public void shouldTreatProjectWithNoTestsAsEmpty() {
    when(project.getTestCompileSourceRoots()).thenReturn(Collections.singletonList(dirWithContents()));
    when(project.getCompileSourceRoots()).thenReturn(Collections.singletonList(nonExistentDir()));
    assertThat(testee.test(project)).isFalse();
  }

  @Test
  public void shouldTreatProjectWithNoCodeAsEmpty() {
    when(project.getTestCompileSourceRoots()).thenReturn(Collections.singletonList(nonExistentDir()));
    when(project.getCompileSourceRoots()).thenReturn(Collections.singletonList(dirWithContents()));
    assertThat(testee.test(project)).isFalse();
  }


  @Test
  public void emptyDirectoriesAreTreatedAsEmpty() {
    when(project.getTestCompileSourceRoots()).thenReturn(Collections.singletonList(emptyDir()));
    when(project.getCompileSourceRoots()).thenReturn(Collections.singletonList(dirWithContents()));
    assertThat(testee.test(project)).isFalse();
  }

  private String dirWithContents() {
    return realDir.getRoot().getAbsolutePath();
  }

  private String emptyDir() {
    return emptyDir.getRoot().getAbsolutePath();
  }
  
  private String nonExistentDir() {
    return new File("ifthisfileexistsbybizarrechancethetestwillfail").getAbsolutePath();
  }


}
