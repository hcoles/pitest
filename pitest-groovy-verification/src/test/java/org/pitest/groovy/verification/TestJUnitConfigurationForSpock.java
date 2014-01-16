package org.pitest.groovy.verification;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.execute.Container;
import org.pitest.execute.DefaultStaticConfig;
import org.pitest.execute.Pitest;
import org.pitest.execute.StaticConfiguration;
import org.pitest.execute.UnGroupedStrategy;
import org.pitest.execute.containers.UnContainer;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestListener;
import org.pitest.testapi.TestResult;

import com.example.spock.SpockTest;
import com.example.spock.ParametrizedSpockTest;

public class TestJUnitConfigurationForSpock {
  
  private final JUnitCompatibleConfiguration testee = new JUnitCompatibleConfiguration(new TestGroupConfig());
  private Pitest                             pitest;
  private Container                          container;

  @Mock
  private TestListener                       listener;
  private StaticConfiguration                staticConfig;

  @Before
  public void createTestee() {
    MockitoAnnotations.initMocks(this);
    this.container = new UnContainer();
    this.staticConfig = new DefaultStaticConfig(new UnGroupedStrategy());
    this.staticConfig.getTestListeners().add(this.listener);
    this.pitest = new Pitest(this.staticConfig);
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
