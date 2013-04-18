package org.pitest.maven;

import static org.junit.Assert.assertEquals;

import org.apache.maven.scm.ScmFileStatus;
import org.junit.Test;

public class ScmStatusTest {

  @Test
  public void shouldMapTheStringAddedToTheRightScmFileStatus() {
    assertEquals(ScmFileStatus.ADDED, ScmStatus.valueOf("ADDED").getStatus());
  }

  @Test
  public void shouldMapTheStringModifiedToTheRightScmFileStatus() {
    assertEquals(ScmFileStatus.MODIFIED, ScmStatus.valueOf("MODIFIED")
        .getStatus());
  }

  @Test
  public void shouldMapTheStringUnknownToTheRightScmFileStatus() {
    assertEquals(ScmFileStatus.UNKNOWN, ScmStatus.valueOf("UNKNOWN")
        .getStatus());
  }

}
