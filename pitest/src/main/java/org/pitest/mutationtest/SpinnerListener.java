package org.pitest.mutationtest;

import java.io.PrintStream;

import org.pitest.mutationtest.instrument.MutationMetaData;

public class SpinnerListener implements MutationResultListener {

  private final static String[] SPINNER_CHARS = new String[] { "\u0008/",
      "\u0008-", "\u0008\\", "\u0008|"       };

  private final PrintStream     out;

  private int                   position      = 0;

  public SpinnerListener(final PrintStream out) {
    this.out = out;
  }

  public void runStart() {

  }

  public void handleMutationResult(final MutationMetaData metaData) {
    this.out.printf("%s", SPINNER_CHARS[this.position % SPINNER_CHARS.length]);
    this.position++;
  }

  public void runEnd() {

  }

}
