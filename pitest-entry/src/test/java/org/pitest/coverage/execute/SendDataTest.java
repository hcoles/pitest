package org.pitest.coverage.execute;

import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.util.SafeDataOutputStream;

public class SendDataTest {

  private SendData             testee;

  private List<String>         testClasses;

  @Mock
  private CoverageOptions      arguments;

  @Mock
  private SafeDataOutputStream os;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testClasses = new ArrayList<String>();
    this.testee = new SendData(this.arguments, this.testClasses);
  }

  @Test
  public void shouldSendArgumentsToMinion() {
    this.testee.apply(this.os);
    verify(this.os).write(this.arguments);
  }

  @Test
  public void shouldSendTestClassesToMinion() {
    this.testClasses.add("foo");
    this.testClasses.add("bar");
    this.testee.apply(this.os);
    verify(this.os).writeInt(this.testClasses.size());
    verify(this.os).writeString("foo");
    verify(this.os).writeString("bar");
  }

}
