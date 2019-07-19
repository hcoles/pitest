package org.pitest.coverage;

public class AlreadyInstrumentedException extends IllegalArgumentException {
  public AlreadyInstrumentedException(String msg) {
    super(msg);
  }
}
