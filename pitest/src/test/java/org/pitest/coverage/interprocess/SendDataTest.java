package org.pitest.coverage.interprocess;

import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.util.SafeDataOutputStream;


public class SendDataTest {
  
  private SendData testee;
  
  
  private  List<String>    testClasses;
  
  @Mock
  private  CoverageOptions arguments;
  
  @Mock
  private SafeDataOutputStream os;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    testClasses = new ArrayList<String>();
    testee = new SendData(arguments, testClasses);
  }
  
  @Test
  public void shouldSendArgumentsToSlave() {
    testee.apply(os);
    verify(this.os).write(arguments);
  }
  
  @Test
  public void shouldSendTestClassesToSlave() {
    testClasses.add("foo");
    testClasses.add("bar");
    testee.apply(os);
    verify(this.os).writeInt(testClasses.size());
    verify(this.os).writeString("foo");
    verify(this.os).writeString("bar"); 
  }

}
