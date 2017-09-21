package org.pitest.sequence;

import java.util.List;

public interface SequenceMatcher<T> {

  boolean matches(List<T> sequence);

  boolean matches(List<T> sequence, Context<T> initialContext);
}
