package org.pitest.coverage;

public class AlreadyInstrumentedException extends IllegalArgumentException {
  private static final long serialVersionUID = 1L;

  public AlreadyInstrumentedException(String msg) {
    super(msg);
  }
}
