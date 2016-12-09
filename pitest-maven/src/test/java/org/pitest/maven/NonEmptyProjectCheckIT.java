package org.pitest.maven;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;

import org.apache.maven.project.MavenProject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@Category(SystemTest.class)
@RunWith(MockitoJUnitRunner.class)
public class NonEmptyProjectCheckIT {

  @Rule
  public TemporaryFolder realDir = new TemporaryFolder();
  
  NonEmptyProjectCheck testee = new NonEmptyProjectCheck();
  
  @Mock
  private MavenProject project;
  
  @Test
  public void shouldTreatProjectWithCodeAndTestsAsNonEmpty() {
    when(project.getTestCompileSourceRoots()).thenReturn(Collections.singletonList(dirThatExists()));
    when(project.getCompileSourceRoots()).thenReturn(Collections.singletonList(dirThatExists()));
    assertThat(testee.apply(project)).isTrue();
  }
  
  @Test
  public void shouldTreatProjectWithNoTestsAsEmpty() {
    when(project.getTestCompileSourceRoots()).thenReturn(Collections.singletonList(dirThatExists()));
    when(project.getCompileSourceRoots()).thenReturn(Collections.singletonList(nonExistentDir()));
    assertThat(testee.apply(project)).isFalse();
  }

  @Test
  public void shouldTreatProjectWithNoCodeAsEmpty() {
    when(project.getTestCompileSourceRoots()).thenReturn(Collections.singletonList(nonExistentDir()));
    when(project.getCompileSourceRoots()).thenReturn(Collections.singletonList(dirThatExists()));
    assertThat(testee.apply(project)).isFalse();
  }
  
  private String dirThatExists() {
    return realDir.getRoot().getAbsolutePath();
  }
  
  private String nonExistentDir() {
    return new File("ifthisfileexistsbybizarrechancethetestwillfail").getAbsolutePath();
  }


}
