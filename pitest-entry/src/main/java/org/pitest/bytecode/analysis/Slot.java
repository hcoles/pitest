package org.pitest.bytecode.analysis;

import org.pitest.functional.predicate.Predicate;

public class Slot<T> implements Predicate<T> {
  private T lastMatched;

  @Override
  public Boolean apply(T t) {
    return (lastMatched != null) && t.equals(lastMatched);
  }

  public void setLastMatched(T t) {
    this.lastMatched = t;
  }
  
  public void reset() {
    lastMatched = null;
  }
 
}
