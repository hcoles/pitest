package org.pitest.mutationtest.instrument;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.Description;
import org.pitest.classinfo.ClassName;
import org.pitest.extension.Configuration;
import org.pitest.extension.ResultCollector;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.execute.MutationStatusTestPair;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.mutationtest.results.MutationResult;
import org.pitest.util.JavaAgent;

public class MutationTestUnitTest {

  private MutationTestUnit      testee;
  private List<MutationDetails> mutations;
  private Collection<ClassName> tests;

  @Mock
  private Configuration         config;

  private MutationConfig        mutationConfig;

  @Mock
  private TimeoutLengthStrategy timeout;

  @Mock
  private JavaAgent             javaAgent;

  @Mock
  private ClassLoader           loader;

  @Mock
  private ResultCollector       rc;

  @Mock
  private MutationEngine        engine;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.mutationConfig = new MutationConfig(this.engine,
        Collections.<String> emptyList());
    this.mutations = new ArrayList<MutationDetails>();
    this.tests = new ArrayList<ClassName>();
    this.testee = new MutationTestUnit(null, this.mutations,
        this.tests, this.config, this.mutationConfig, this.javaAgent,
        this.timeout, false, null);
  }

  @Test
  public void shouldNotRunWhenNoMutationsAvailable() {
    this.testee.execute(this.loader, this.rc);
    verify(this.rc).notifySkipped(any(Description.class));
  }

  @Test
  public void shouldReportWhenMutationsNotCoveredByAnyTest() {
    addMutation();
    this.tests.add(new ClassName("foo"));
    this.testee.execute(this.loader, this.rc);
    final MutationMetaData metaData = makeMetaData(this.mutations.get(0),
        new MutationStatusTestPair(0, DetectionStatus.NO_COVERAGE));
    verify(this.rc).notifyEnd(any(Description.class), eq(metaData));
  }

  private MutationMetaData makeMetaData(final MutationDetails details,
      final MutationStatusTestPair status) {
    return new MutationMetaData(this.mutationConfig.getMutatorNames(),
        Collections.singletonList(new MutationResult(details, status)));
  }

  private void addMutation() {
    this.mutations.add(new MutationDetails(new MutationIdentifier(Location.location("foo","method","()V"), 1,
        "foo"), null, null, 0, 0));
  }

}
