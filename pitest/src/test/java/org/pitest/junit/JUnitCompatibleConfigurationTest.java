package org.pitest.junit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.pitest.testapi.TestGroupConfig;

public class JUnitCompatibleConfigurationTest {
  JUnitCompatibleConfiguration testee;

  @Before
  public void setUp() throws Exception {

    this.testee = new JUnitCompatibleConfiguration(new TestGroupConfig(), Collections.<String>emptyList());
  }

  @Test
  public void considersPre46Invalid() throws Exception {
    assertThat(this.testee.isInvalidVersion("4.5"), is(true));
    assertThat(this.testee.isInvalidVersion("4.5-SNAPSHOT"), is(true));
  }

  @Test
  public void canParseReleaseVersion() throws Exception {
    assertThat(this.testee.isInvalidVersion("4.6"), is(false));
    assertThat(this.testee.isInvalidVersion("4.10"), is(false));
  }

  @Test
  public void canParseSnapshotVersion() throws Exception {
    assertThat(this.testee.isInvalidVersion("4.10-SNAPSHOT"), is(false));
    assertThat(this.testee.isInvalidVersion("4.5-SNAPSHOT"), is(true));
  }

  @Test
  public void canParseReleaseCandidateVersion() throws Exception {
    assertThat(this.testee.isInvalidVersion("4.10.rc1"), is(false));
  }
}