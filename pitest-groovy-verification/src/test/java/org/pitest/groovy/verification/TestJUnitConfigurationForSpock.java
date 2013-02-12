package org.pitest.groovy.verification;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.DefaultStaticConfig;
import org.pitest.Pitest;
import org.pitest.TestResult;
import org.pitest.containers.UnContainer;
import org.pitest.extension.Container;
import org.pitest.extension.StaticConfiguration;
import org.pitest.extension.TestListener;
import org.pitest.extension.common.UnGroupedStrategy;
import org.pitest.junit.JUnitCompatibleConfiguration;

import com.example.spock.SpockTest;
import com.example.spock.ParametrizedSpockTest;

public class TestJUnitConfigurationForSpock {
  
  private final JUnitCompatibleConfiguration testee = new JUnitCompatibleConfiguration();
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
