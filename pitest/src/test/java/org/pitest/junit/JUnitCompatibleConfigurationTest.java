package org.pitest.junit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.pitest.testapi.TestGroupConfig;

public class JUnitCompatibleConfigurationTest {
  JUnitCompatibleConfiguration testee;

  @Before
  public void setUp() throws Exception {

    this.testee = new JUnitCompatibleConfiguration(new TestGroupConfig(), Collections.<String>emptyList(),
            Collections.<String>emptyList());
  }

  @Test
  public void considersPre46Invalid() throws Exception {
    assertThat(this.testee.isInvalidVersion("4.5")).isTrue();
    assertThat(this.testee.isInvalidVersion("4.5-SNAPSHOT")).isTrue();
  }

  @Test
  public void canParseReleaseVersion() throws Exception {
    assertThat(this.testee.isInvalidVersion("4.6")).isFalse();
    assertThat(this.testee.isInvalidVersion("4.10")).isFalse();
  }

  @Test
  public void canParseSnapshotVersion() throws Exception {
    assertThat(this.testee.isInvalidVersion("4.10-SNAPSHOT")).isFalse();
    assertThat(this.testee.isInvalidVersion("4.5-SNAPSHOT")).isTrue();
  }

  @Test
  public void canParseReleaseCandidateVersion() throws Exception {
    assertThat(this.testee.isInvalidVersion("4.10.rc1")).isFalse();
  }
}
