package org.pitest.testapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
import org.pitest.util.IsolationUtils;

public class DescriptionTest {

  private Description testee;

  @Test
  public void shouldCloneViaXStreamWithoutError() throws Exception {
    try {
      this.testee = new Description("foo", IOException.class);
      final Description actual = (Description) IsolationUtils
          .clone(this.testee);

      assertEquals(this.testee, actual);
    } catch (final Throwable t) {
      fail(t.getMessage());
    }
  }

}
