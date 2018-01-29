package org.pitest.aggregate;

public class ReportAggregationException extends Exception {

  private static final long serialVersionUID = 1L;

  public ReportAggregationException(final String message) {
    super(message);
  }

  public ReportAggregationException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
