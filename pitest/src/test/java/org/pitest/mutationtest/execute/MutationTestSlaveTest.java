package org.pitest.mutationtest.execute;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.PitError;
import org.pitest.classinfo.ClassName;
import org.pitest.extension.Configuration;
import org.pitest.internal.ClassByteArraySource;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.instrument.TimeoutLengthStrategy;
import org.pitest.util.ExitCode;
import org.pitest.util.SafeDataInputStream;


public class MutationTestSlaveTest {
  
  private MutationTestSlave testee;
  
  @Mock
  private Reporter reporter;

  @Mock
  private SafeDataInputStream is;
  
  @Mock
  private MutationEngine engine;
  
  @Mock
  private TimeoutLengthStrategy timeoutStrategy;
  
  @Mock
  private Configuration testConfig;
  
  @Mock
  private Mutater mutater;
  
  private SlaveArguments args;
  
  private Collection<MutationDetails> mutations;
  
  private Collection<ClassName> tests;



    
  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    mutations = new ArrayList<MutationDetails>();
    tests = new ArrayList<ClassName>();
    
    args = new SlaveArguments(mutations, tests, engine, timeoutStrategy, false, testConfig);
    
    when(is.read(SlaveArguments.class)).thenReturn(args);
    when(engine.createMutator(any(ClassByteArraySource.class))).thenReturn(mutater);
    
    testee = new MutationTestSlave(is, reporter);
  }
  
  @Test
  public void shouldReportNoErrorWhenNoMutationsSupplied() {
    testee.run();
    verify(reporter).done(ExitCode.OK);
  }
  
  @Test
  public void shouldReportErrorWhenOneOccursDuringAnalysis() {
    mutations.add(new MutationDetails(new MutationIdentifier("foo",1, "foo"),null,null,null,0,0));
    when(mutater.getMutation(any(MutationIdentifier.class))).thenThrow(new PitError("foo"));
    testee.run();
    verify(reporter).done(ExitCode.UNKNOWN_ERROR);
  }


}
