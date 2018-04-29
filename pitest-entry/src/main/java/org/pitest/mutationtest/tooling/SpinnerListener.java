package org.pitest.mutationtest.tooling;

import java.io.PrintStream;

import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.MutationResultListener;

public class SpinnerListener implements MutationResultListener {

  private static final String[] SPINNER_CHARS = new String[] { "\u0008/",
    "\u0008-", "\u0008\\", "\u0008|"       };

  private final PrintStream     out;

  private int                   position      = 0;

  public SpinnerListener(final PrintStream out) {
    this.out = out;
  }

  @Override
  public void runStart() {

  }

  @Override
  public void handleMutationResult(final ClassMutationResults metaData) {
    this.out.printf("%s", SPINNER_CHARS[this.position % SPINNER_CHARS.length]);
    this.position++;
  }

  @Override
  public void runEnd() {

  }

}
