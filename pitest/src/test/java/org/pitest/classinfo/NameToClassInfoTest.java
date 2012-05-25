package org.pitest.classinfo;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class NameToClassInfoTest {
  
  @Test
  public void shouldFetchClassWhenApplied() {
    Repository repository = mock(Repository.class);
    NameToClassInfo testee = new NameToClassInfo(repository);
    testee.apply("foo");
    verify(repository).fetchClass("foo");
  }
  
}
