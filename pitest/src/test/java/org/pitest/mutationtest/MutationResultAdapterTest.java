package org.pitest.mutationtest;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.mutationtest.report.MutationTestResultMother;

public class MutationResultAdapterTest {

  private MutationResultAdapter  testee;

  @Mock
  private MutationResultListener child;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new MutationResultAdapter(this.child);
  }

  @Test
  public void shouldAdaptRunStart() {
    this.testee.onRunStart();
    verify(this.child).runStart();
  }

  @Test
  public void shouldAdaptRunEnd() {
    this.testee.onRunEnd();
    verify(this.child).runEnd();
  }

  @Test
  public void shouldExtractMetaDataWhenTestSuccess() {
    final MutationResult mr = makeResult();
    final MutationMetaData metaData = MutationTestResultMother
        .createMetaData(mr);
    this.testee.onTestSuccess(MutationTestResultMother
        .createResult(MutationTestResultMother.createMetaData(mr)));
    verify(this.child).handleMutationResult(metaData);
  }

  @Test
  public void shouldExtractMetaDataWhenTestError() {
    final MutationResult mr = makeResult();
    final MutationMetaData metaData = MutationTestResultMother
        .createMetaData(mr);
    this.testee.onTestError(MutationTestResultMother
        .createResult(MutationTestResultMother.createMetaData(mr)));
    verify(this.child).handleMutationResult(metaData);
  }

  @Test
  public void shouldExtractMetaDataWhenTestFailure() {
    final MutationResult mr = makeResult();
    final MutationMetaData metaData = MutationTestResultMother
        .createMetaData(mr);
    this.testee.onTestFailure(MutationTestResultMother
        .createResult(MutationTestResultMother.createMetaData(mr)));
    verify(this.child).handleMutationResult(metaData);
  }

  private MutationResult makeResult() {
    final MutationResult mr = new MutationResult(
        MutationTestResultMother.createDetails(), new MutationStatusTestPair(0,
            DetectionStatus.KILLED));
    return mr;
  }

}
