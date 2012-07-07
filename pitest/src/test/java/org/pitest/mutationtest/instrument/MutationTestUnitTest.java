package org.pitest.mutationtest.instrument;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.Description;
import org.pitest.PitError;
import org.pitest.classinfo.ClassName;
import org.pitest.extension.Configuration;
import org.pitest.extension.ResultCollector;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.execute.MutationStatusTestPair;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.mutationtest.results.MutationResult;
import org.pitest.util.JavaAgent;


public class MutationTestUnitTest {
  
  private MutationTestUnit testee;
  private List<MutationDetails> mutations;
  private Collection<ClassName> tests;
  
  private String classPath;
  private File baseDir;
  
  @Mock
  private Configuration config;
  
  private MutationConfig mutationConfig;
  
  @Mock
  private TimeoutLengthStrategy timeout;
  
  @Mock
  private JavaAgent javaAgent;
  
  @Mock
  private ClassLoader loader;
  
  @Mock
  private ResultCollector rc;
  
  @Mock
  private MutationEngine engine;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    mutationConfig = new MutationConfig(engine, Collections.<String>emptyList());
    mutations = new ArrayList<MutationDetails>();
    tests = new ArrayList<ClassName>();
    testee = new MutationTestUnit(baseDir, mutations, tests, config, mutationConfig, javaAgent, timeout, false, classPath);
  }

  
  @Test
  public void shouldNotRunWhenNoMutationsAvailable() {
    testee.execute(loader, rc);
    verify(rc).notifySkipped(any(Description.class));
  }
  
  
  @Test
  public void shouldReportWhenMutationsNotCoveredByAnyTest() {
    addMutation();
    tests.add(new ClassName("foo"));
    testee.execute(loader, rc);
    MutationMetaData metaData = makeMetaData(mutations.get(0), new MutationStatusTestPair(0, DetectionStatus.NO_COVERAGE));
    verify(rc).notifyEnd(any(Description.class), eq(metaData));
  }
  
  


  private MutationMetaData makeMetaData(MutationDetails details,MutationStatusTestPair status ) {
    return new MutationMetaData(mutationConfig.getMutatorNames(), Collections.singletonList(new MutationResult(details,status)));
  }


  @Test
  public void shouldReportErrorWhenCannotLaunchChildProcess() {
    when(javaAgent.getJarLocation()).thenThrow(new PitError("oops"));

  }


  private void addMutation() {
    mutations.add(new MutationDetails(new MutationIdentifier("foo",1, "foo"),null,null,null,0, 0));
  }
  
}
