package org.pitest.groovy.verification;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.testapi.execute.Container;
import org.pitest.testapi.execute.Pitest;
import org.pitest.testapi.execute.containers.UnContainer;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestListener;
import org.pitest.testapi.TestResult;

import com.example.spock.SpockTest;
import com.example.spock.ParametrizedSpockTest;

public class TestJUnitConfigurationForSpock {
  
  private final JUnitCompatibleConfiguration testee = new JUnitCompatibleConfiguration(new TestGroupConfig(), Collections.<String>emptyList());
  private Pitest                             pitest;
  private Container                          container;

  @Mock
  private TestListener                       listener;

  @Before
  public void createTestee() {
    MockitoAnnotations.initMocks(this);
    this.container = new UnContainer();
    this.pitest = new Pitest(this.listener);
  }

  @Test
  public void shouldFindTestInSpockTest() {
    run(SpockTest.class);
    verify(this.listener, times(1)).onTestSuccess(any(TestResult.class));
  }
  
  @Test
  public void shouldFindTestsInParameterisedSpockTest() {
    run(ParametrizedSpockTest.class);
    verify(this.listener, times(3)).onTestSuccess(any(TestResult.class));
  }
  
  private void run(final Class<?> clazz) {
    this.pitest.run(this.container, this.testee, clazz);
  }

}
