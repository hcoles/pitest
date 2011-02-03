package org.pitest;

import org.apache.maven.plugin.Mojo;
import org.codehaus.plexus.PlexusTestCase;

public class PitMojoTest extends PlexusTestCase {

  private PitMojo testee;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.testee = (PitMojo) lookup(Mojo.ROLE);
  }

  public void testCanCreateMojo() {
    assertNotNull(this.testee);
  }

}
