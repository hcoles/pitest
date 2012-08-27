package org.pitest.mutationtest;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.mutationtest.execute.MutationStatusTestPair;
import org.pitest.mutationtest.instrument.MutationMetaData;
import org.pitest.mutationtest.report.MutationTestResultMother;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.mutationtest.results.MutationResult;

public class MutationResultAdapterTest {
  
  private MutationResultAdapter testee;
  
  @Mock
  private MutationResultListener child;


  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    testee = new MutationResultAdapter(child);
  }
  
  @Test
  public void shouldAdaptRunStart() {
    testee.onRunStart();
    verify(child).runStart();
  }
  
  @Test
  public void shouldAdaptRunEnd() {
    testee.onRunEnd();
    verify(child).runEnd();
  }
  
  @Test
  public void shouldExtractMetaDataWhenTestSuccess() {
    final MutationResult mr = makeResult();
    MutationMetaData metaData = MutationTestResultMother
        .createMetaData(mr);
    testee.onTestSuccess(MutationTestResultMother.createResult(MutationTestResultMother
        .createMetaData(mr)));
    verify(child).handleMutationResult(metaData);
  }
  
  @Test
  public void shouldExtractMetaDataWhenTestError() {
    final MutationResult mr = makeResult();
    MutationMetaData metaData = MutationTestResultMother
        .createMetaData(mr);
    testee.onTestError(MutationTestResultMother.createResult(MutationTestResultMother
        .createMetaData(mr)));
    verify(child).handleMutationResult(metaData);
  }
  
  @Test
  public void shouldExtractMetaDataWhenTestFailure() {
    final MutationResult mr = makeResult();
    MutationMetaData metaData = MutationTestResultMother
        .createMetaData(mr);
    testee.onTestFailure(MutationTestResultMother.createResult(MutationTestResultMother
        .createMetaData(mr)));
    verify(child).handleMutationResult(metaData);
  }

  private MutationResult makeResult() {
    final MutationResult mr = new MutationResult(
        MutationTestResultMother.createDetails(), new MutationStatusTestPair(0,
            DetectionStatus.KILLED));
    return mr;
  }

  
}
