package org.pitest.mutationtest;

import org.pitest.functional.Option;
import org.pitest.testapi.Description;
import org.pitest.testapi.TestListener;
import org.pitest.testapi.TestResult;

public class MutationResultAdapter implements TestListener {

  private final MutationResultListener child;

  public MutationResultAdapter(final MutationResultListener child) {
    this.child = child;
  }

  public static TestListener adapt(final MutationResultListener child) {
    return new MutationResultAdapter(child);
  }

  public void onRunStart() {
    this.child.runStart();
  }

  public void onTestStart(final Description d) {
  }

  public void onTestFailure(final TestResult tr) {
    extractMutationData(tr);
  }

  public void onTestError(final TestResult tr) {
    extractMutationData(tr);
  }

  public void onTestSkipped(final TestResult tr) {
  }

  public void onTestSuccess(final TestResult tr) {
    extractMutationData(tr);
  }

  public void onRunEnd() {
    this.child.runEnd();
  }

  private void extractMutationData(final TestResult tr) {
    for (final MutationMetaData metaData : extractMetaData(tr)) {
      this.child.handleMutationResult(metaData);
    }
  }

  private Option<MutationMetaData> extractMetaData(final TestResult tr) {
    return tr.getValue(MutationMetaData.class);
  }

}
