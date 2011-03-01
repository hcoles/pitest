package org.pitest.internal;

public class SerializationException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public SerializationException(final Exception ex) {
    super(ex);
  }

}
