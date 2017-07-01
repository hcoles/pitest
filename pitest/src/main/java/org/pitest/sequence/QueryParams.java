package org.pitest.sequence;

public final class QueryParams<T> {
  
  private final Match<T> ignoring;
  private final boolean debug;
  
  QueryParams(Match<T> ignoring, boolean debug) {
    this.ignoring = ignoring;
    this.debug = debug;
  }
  
  public static <T> QueryParams<T> params(Class<T> clazz) {
    return params();
  }
  
  public static <T> QueryParams<T> params() {
    return new QueryParams<T>(Match.<T>never(), false);
  }
  
  public QueryParams<T> withIgnores(Match<T> ignore) {
    return new QueryParams<T>(ignore, debug);
  }
  
  public QueryParams<T> withDebug(boolean debug) {
    return new QueryParams<T>(ignoring, debug);
  }
  
  public Match<T> ignoring() {
    return ignoring;
  }

  public boolean isDebug() {
    return debug;
  }

  
}
